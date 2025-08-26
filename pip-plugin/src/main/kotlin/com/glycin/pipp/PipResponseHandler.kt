package com.glycin.pipp

import com.glycin.pipp.http.CodeOperation
import com.glycin.pipp.http.PipPrankRequestBody
import com.glycin.pipp.http.PipResponse
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.http.PrankType
import com.glycin.pipp.utils.NanoId
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.endOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val FAIL_RESPONSE = "I'm sleeping now, leave me alone."

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
    private val scope: CoroutineScope,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
) {

    fun processMusicResponse(pipResponse: PipResponse) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.SITTING)
            delay(1500)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(10_000)
            agentComponent.hideSpeechBubble()
        }
    }

    fun processChatResponse(pipResponse: PipResponse) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.WALL_SHOOTING) //TODO:
            delay(500)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(30_000)
            agentComponent.hideSpeechBubble()
        }
    }

    fun processCodingResponse(pipResponse: PipResponse, originalInput: String, chatId: String?) {
        if(pipResponse.isFailResponse()) {
            processFailResponse()
            return
        }

        if(pipResponse.prankType.isNullOrEmpty()){
            processAcceptedCoding(pipResponse)
        } else {
            processPrank(pipResponse, originalInput, chatId ?: NanoId.generate())
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
                    println(cf)
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
            pip.changeStateTo(PipState.SITTING)
            agentComponent.showSpeechBubble(response.response)
        }
    }

    private fun processPrank(response: PipResponse, originalInput: String, chatId: String) {
        println("PROCESSING PRANK ${response.prankType}")
        scope.launch(Dispatchers.Default) {
            pip.changeStateTo(PipState.SITTING)
            agentComponent.showSpeechBubble(response.response)
            delay(3000)

            val prankType = PrankType.valueOf(response.prankType ?: "OBFUSCATE")

            val requestBody = PipPrankRequestBody(
                type = prankType ,
                originalInput = originalInput,
                reason = response.response,
                context = editor.document.text,
                chatId = chatId,
            )

            when(prankType) {
                PrankType.TRANSLATE, PrankType.OBFUSCATE -> {
                    PipRestClient.doPrank(requestBody)?.let {
                        agentComponent.hideSpeechBubble()
                        delay(3000)
                        agentComponent.showSpeechBubble(it.response)
                        TextWriter.replaceText(0, editor.document.textLength, it.code, editor.document, project)
                    }
                }
                PrankType.EXPLODE -> {
                    // TODO: Do kaboom
                    delay(3000)
                    agentComponent.hideSpeechBubble()
                }
            }

            delay(1000)
            pip.changeStateTo(PipState.SITTING) //TODO: Add pranking animations
        }
    }

    private fun processFailResponse() {
        scope.launch(Dispatchers.Default) {
            delay(500)
            pip.changeStateTo(PipState.SLEEPING)
        }
    }

    private fun PipResponse.isFailResponse() = this.response == FAIL_RESPONSE
}

private data class CodeReadResult(
    val operation: CodeOperation,
    val startOffset: Int,
    val endOffset: Int,
    val code: String,
    val document: Document,
)