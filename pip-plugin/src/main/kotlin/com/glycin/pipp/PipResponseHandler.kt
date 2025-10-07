package com.glycin.pipp

import com.glycin.pipp.explosion.BoomManager
import com.glycin.pipp.http.CodeOperation
import com.glycin.pipp.http.PipPrankRequestBody
import com.glycin.pipp.http.PipResponse
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.http.PrankType
import com.glycin.pipp.pong.PongGame
import com.glycin.pipp.settings.PipSettings
import com.glycin.pipp.tictactoe.TicTacToeStarter
import com.glycin.pipp.ui.DvdComponent
import com.glycin.pipp.utils.NanoId
import com.glycin.pipp.utils.TextWriter
import com.glycin.pipp.utils.showPngInPopup
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollingModel
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.endOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Point
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.min

private const val FAIL_RESPONSE = "I'm sleeping now, leave me alone."

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
    private val scope: CoroutineScope,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
    private val maxX: Float,
    private val maxY: Float,
    private val pipSettings: PipSettings,
) {

    fun processMusicResponse(pipResponse: PipResponse) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.METAL)
            delay(1500)
            agentComponent.showSpeechBubble(pipResponse.response, false)
            agentComponent.showCatJam()
            delay(20_000)
            agentComponent.hideSpeechBubble(false)
            delay(3000)
            agentComponent.hideCatJam()
        }
    }

    fun processChatResponse(pipResponse: PipResponse) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.TALKING)
            pipResponse.showMeme()
            delay(500)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(20_000)
            agentComponent.hideSpeechBubble()
        }
    }

    fun processCodingResponse(pipResponse: PipResponse, originalInput: String, chatId: String?) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        pipResponse.showMeme()

        if(pipResponse.prankType.isNullOrEmpty()){
            processAcceptedCoding(pipResponse)
        } else {
            processPrank(pipResponse, originalInput, chatId ?: NanoId.generate())
        }
    }

    fun processGamingResponse(pipResponse: PipResponse) {
        pipResponse.showMeme()

        when(pipResponse.gameName) {
            "PONG" -> {
                scope.launch(Dispatchers.Default) {
                    pip.changeStateTo(PipState.TALKING)
                    agentComponent.showSpeechBubble(pipResponse.response)
                    delay(3000)
                    pip.moveTo(Vec2(maxX - (maxX / 3f), pip.position.y), 1500, endAnimationState = PipState.PONG) // TODO: Make ping pong animation
                    delay(2000)
                    PongGame(project, editor, scope) {
                        pip.moveTo(Vec2(maxX, maxY), 1500)
                    }.initGame()
                }
            }
            "TIC-TAC-TOE" -> {
                scope.launch(Dispatchers.Default) {
                    agentComponent.showSpeechBubble(pipResponse.response)
                    delay(10_000)
                    agentComponent.hideSpeechBubble(false)
                    pip.changeStateTo(PipState.YOYO)
                    TicTacToeStarter(
                        project = project,
                        scope = scope,
                        chatId = NanoId.generate(),
                        pip = pip,
                        agentComponent = agentComponent
                    ).showTicTacToe()
                }
            }
            else -> {
                scope.launch(Dispatchers.Default) {
                    pip.changeStateTo(PipState.YOYO)
                    agentComponent.showSpeechBubble(pipResponse.response)
                    delay(15_000)
                    agentComponent.hideSpeechBubble()
                }
            }
        }
    }

    fun processStuckResponse(pipResponse: PipResponse, contentComponent: JComponent, scrollModel: ScrollingModel?, fps: Long) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        scope.launch(Dispatchers.EDT) {
            pipResponse.showMeme()
            delay(1000)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(5_000)

            if(scrollModel != null) {
                val visibleArea = scrollModel.visibleArea
                val dvdComponent = DvdComponent(visibleArea.width, visibleArea.height, scope, fps) {
                    contentComponent.remove(it)
                    contentComponent.repaint()
                    contentComponent.revalidate()
                    pip.changeStateTo(PipState.IDLE)
                }.also {
                    it.bounds = visibleArea
                    it.isOpaque = false
                }
                contentComponent.add(dvdComponent)
                contentComponent.repaint()
                contentComponent.revalidate()
            }
            delay(10_000)
            agentComponent.hideSpeechBubble()
            pip.changeStateTo(PipState.SLEEPING)
        }
    }

    private fun processAcceptedCoding(response: PipResponse) {
        val codeFragments = response.code ?: emptyList()
        val cache = PsiShortNamesCache.getInstance(project)
        val searchScope = GlobalSearchScope.allScope(project)

        scope.launch(Dispatchers.Default) {
            delay(500)
            val results = readAction {
                codeFragments.mapNotNull { cf ->
                    val psiClass = cache.getClassesByName(cf.className, searchScope).singleOrNull()
                    val psiMethod = psiClass?.methods?.firstOrNull { it.name == cf.methodName }

                    if(psiClass == null) return@mapNotNull null
                    PsiDocumentManager.getInstance(project).getDocument(psiClass.containingFile)?.let { document ->
                        val validatedLine = min(document.lineCount, cf.line - 1)
                        val startOffset = document.getLineStartOffset(validatedLine)
                        val validatedEndOffset = psiMethod?.endOffset ?: document.textLength

                        CodeReadResult(
                            operation = cf.operation,
                            startOffset = startOffset,
                            endOffset = validatedEndOffset,
                            code = cf.code,
                            document = document
                        )
                    }
                }
            }

            delay(500)

            results.forEach { result ->
                with(result) {
                    when(operation) {
                        CodeOperation.INSERT -> TextWriter.writeText(startOffset, code, document, project)
                        CodeOperation.REPLACE -> TextWriter.replaceText(startOffset, endOffset, code, document, project)
                    }
                    WriteCommandAction.runWriteCommandAction(project) {
                        PsiDocumentManager.getInstance(project).commitDocument(document)
                    }
                }
            }

            delay(1000)
            agentComponent.showSpeechBubble(response.response)
            delay(20_000)
            agentComponent.hideSpeechBubble()
        }
    }

    private fun processPrank(response: PipResponse, originalInput: String, chatId: String) {
        println("PROCESSING PRANK ${response.prankType}")
        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.SITTING)
            agentComponent.showSpeechBubble(response.response)
            delay(3000)

            val prankType = PrankType.valueOf(response.prankType ?: "TRANSLATE")

            val requestBody = PipPrankRequestBody(
                type = prankType ,
                originalInput = originalInput,
                reason = response.response,
                context = editor.document.text,
                chatId = chatId,
            )

            when(prankType) {
                PrankType.TRANSLATE -> {
                    PipRestClient.doPrank(requestBody)?.let {
                        agentComponent.hideSpeechBubble()
                        pip.moveTo(Vec2(maxX / 2, pip.position.y), 3000)
                        delay(5000)
                        TextWriter.replaceText(0, editor.document.textLength, it.code, editor.document, project)
                        delay(500)
                        pip.changeStateTo(PipState.DEAL_WITH_IT)
                        pip.face(Facing.RIGHT)
                        delay(10_000)
                        agentComponent.showSpeechBubble(it.response)
                    }
                }

                PrankType.POETRY -> {
                    PipRestClient.doPrank(requestBody)?.let {
                        agentComponent.hideSpeechBubble()
                        pip.moveTo(Vec2(maxX / 2, pip.position.y), 3000)
                        delay(3500)
                        pip.changeStateTo(PipState.MAGIC)
                        delay(3000)
                        TextWriter.replaceText(0, editor.document.textLength, it.code, editor.document, project)
                        delay(1500)
                        agentComponent.showSpeechBubble(it.response)
                        delay(20_000)
                    }
                }

                PrankType.EXPLODE -> {
                    val boomManager = BoomManager(scope)
                    val targets = runBlocking(Dispatchers.EDT) {
                        randomVisibleLines()
                    }
                    val pipClimbMargin = (pip.width / 2) - 20
                    agentComponent.hideSpeechBubble(false)
                    delay(2000)
                    pip.moveTo(Vec2(maxX + pipClimbMargin, maxY), 3000)
                    delay(4500)
                    targets.forEach { tar ->
                        pip.moveTo(Vec2(maxX + pipClimbMargin, tar.y.toFloat()), 1000, PipState.CLIMBING, PipState.HANG_IDLE)
                        delay(3000)
                        pip.changeStateTo(PipState.WALL_SHOOTING)
                        delay(900)
                        pip.changeStateTo(PipState.HANG_IDLE)
                        runBlocking(Dispatchers.EDT) {
                            boomManager.explode(tar, editor)
                        }
                        delay(3000)
                    }
                    delay(500)
                    pip.moveTo(Vec2(maxX + pipClimbMargin, maxY), 3000, PipState.CLIMBING, PipState.HANG_IDLE)
                    delay(3200)
                    pip.moveTo(Vec2(maxX - 50, maxY), 1500)
                    delay(2000)
                    agentComponent.showSpeechBubble("You're welcome!")
                }
            }

            delay(20_000)
            agentComponent.hideSpeechBubble()
        }
    }

    private fun processFailResponse() {
        scope.launch(Dispatchers.Default) {
            delay(500)
            pip.changeStateTo(PipState.SLEEPING)
        }
    }

    private fun randomVisibleLines(): List<Point> {
        val doc = editor.document
        if (doc.lineCount == 0) return emptyList()
        val visible = editor.scrollingModel.visibleArea
        val topVisualLine = editor.yToVisualLine(visible.y)
        val bottomVisualLine = max(topVisualLine, (editor.yToVisualLine(visible.y + visible.height)) - 5)
        return (1..5).map {
            val line = IntRange(topVisualLine, bottomVisualLine).random()
            val lineOffset = (editor.document.getLineStartOffset(line) + editor.document.getLineEndOffset(line)) / 2
            editor.offsetToXY(lineOffset)
        }
    }

    private fun PipResponse.isFailResponse() = this.response == FAIL_RESPONSE

    private fun PipResponse.showMeme() {
        if(memeFileName.isNullOrEmpty()) return
        val path = "${pipSettings.state.memeSaveFolder}\\$memeFileName"
        println("Showing meme at path $path")
        scope.launch(Dispatchers.EDT) {
            showPngInPopup(agentComponent, path, listOf("I'm so funny", "Back to the 2010s!", "Miaouw?").random())
        }
    }
}

private data class CodeReadResult(
    val operation: CodeOperation,
    val startOffset: Int,
    val endOffset: Int,
    val code: String,
    val document: Document,
)