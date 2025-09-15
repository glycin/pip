package com.glycin.pipp

import com.glycin.pipp.context.*
import com.glycin.pipp.http.CategorizationDto
import com.glycin.pipp.http.PipPasteBody
import com.glycin.pipp.http.PipRequestBody
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.http.RequestCategory
import com.glycin.pipp.paste.PasteHandler
import com.glycin.pipp.paste.PipPasteResponseHandler
import com.glycin.pipp.prompts.CodingPrompts
import com.glycin.pipp.settings.PipSettings
import com.glycin.pipp.tictactoe.TicTacToeStarter
import com.glycin.pipp.ui.DvdComponent
import com.glycin.pipp.ui.PipInputDialog
import com.glycin.pipp.utils.Extensions.addCategory
import com.glycin.pipp.utils.Extensions.fqMethodName
import com.glycin.pipp.utils.Extensions.getSelectedJavaDeclarations
import com.glycin.pipp.utils.NanoId
import com.glycin.pipp.utils.TextWriter
import com.google.gson.GsonBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.File
import javax.swing.JComponent


private const val FPS = 120L
private const val MARGIN_X = 5f
private const val MARGIN_Y = 35f

class Manager(
    private val scope: CoroutineScope,
    private val project: Project,
    private val pipSettings: PipSettings,
): Disposable {

    private val editor = FileEditorManager.getInstance(project).selectedTextEditor
    private val contentComponent = editor?.contentComponent as JComponent
    private val scrollModel = editor?.scrollingModel
    private val pip = Pip(
        position = Vec2.zero,
        scope = scope
    )
    private val agentComponent: AgentComponent = AgentComponent(pip, scope, FPS).also {
        it.bounds = contentComponent.bounds
        it.isOpaque = false
    }

    private val chatIds = mutableListOf(NanoId.generate())
    private lateinit var pasteHandler: PasteHandler

    private var maxX : Float = 0.0f
    private var maxY : Float = 0.0f

    init {
        /*val introComponent = IntroComponent(scope).also {
            it.bounds = contentComponent.bounds
            it.isOpaque = false
        }*/

        contentComponent.let {
            it.add(agentComponent)
            //it.add(introComponent)
            it.revalidate()
            it.repaint()
            it.addComponentListener(object : ComponentListener {
                override fun componentResized(e: ComponentEvent?) {
                    agentComponent.bounds = it.bounds
                }

                override fun componentMoved(e: ComponentEvent?) {}
                override fun componentShown(e: ComponentEvent?) {}
                override fun componentHidden(e: ComponentEvent?) {}
            })
        }

        scope.launch(Dispatchers.EDT) {
            val visibleArea = scrollModel?.visibleArea!!
            val dvdComponent = DvdComponent(visibleArea.width,visibleArea.height,scope, FPS).also {
                it.bounds = contentComponent.bounds
                it.isOpaque = false
            }
            contentComponent.add(dvdComponent)
            scrollModel.addVisibleAreaListener {
                maxX = (it.newRectangle.width - pip.width - MARGIN_X) + it.newRectangle.x
                maxY = (it.newRectangle.height - pip.height + MARGIN_Y) + it.newRectangle.y
                pip.anchor(maxX, maxY)
            }

            maxX = visibleArea.width - pip.width - MARGIN_X
            maxY = visibleArea.height - pip.height.toFloat() + MARGIN_Y
            pip.position = Vec2(maxX, maxY)
        }
        attachPasteHandler()
        //introComponent.showIntro() //TODO: SHow intro
    }

    fun showInput() {
        val dialog = PipInputDialog(project)

        if(!dialog.showAndGet() || dialog.userInput.isEmpty() || editor == null) {
            return
        }

        val chatId = if(dialog.newChat) NanoId.generate().also { chatIds.add(it) } else chatIds.last()
        val responseHandler = PipResponseHandler(editor, project, scope, pip, agentComponent, maxX, maxY, pipSettings)
        val requestBody = PipRequestBody(
            input = dialog.userInput,
            think = dialog.think,
            chatId =  chatId
        )

        scope.launch(Dispatchers.Default) {
            if(dialog.stream) {
                val streamResponseHandler = PipStreamResponseHandler(editor, project, pip)
                TextWriter.deleteText(0, editor.document.textLength, editor.document, project)
                PipRestClient.doCodeQuestionStream(
                    pipRequest = PipRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(dialog.userInput, ""),
                        think = dialog.think,
                        chatId =  chatId
                    )
                ).collect { e -> streamResponseHandler.processSse(e) }
            } else {
                pip.changeStateTo(PipState.THINKING)
                PipRestClient.getCategory(requestBody)?.let {
                    when(it.category) {
                        RequestCategory.JUST_CHATTING -> handleChattingRequest(requestBody, it, responseHandler)
                        RequestCategory.CODING -> handleCodingRequest(requestBody, it, responseHandler)
                        RequestCategory.GAMES ->  handleGamingRequest(requestBody, it, responseHandler)
                        RequestCategory.MUSIC -> handleMusicRequest(requestBody, it, responseHandler)
                        RequestCategory.BUTLER -> handleButlerRequest(requestBody, it, responseHandler)
                    }
                }
            }
        }
    }

    fun showAndDoContextReload() {
        DumbService.getInstance(project).runReadActionInSmartMode {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    val (nodes, edges) = CodeGraphBuilder(project).build()
                    val json = GsonBuilder()
                        .disableHtmlEscaping()
                        .setPrettyPrinting()
                        .create()
                        .toJson(mapOf("nodes" to nodes, "links" to edges))
                    pipSettings.state.jsonExportPath?.let { path ->
                        val file = File(path)
                        file.writeText(json)
                    }
                },
                "Building PSI Graph",
                true,
                project
            )
        }
    }

    fun interceptPaste(text: String, line: Int, offset: Int) {
        if (editor?.document == null) return

        pip.changeStateTo(PipState.STOP)
        val requestBody = PipPasteBody(
            pasteText = text,
            pasteLine = line,
            document = editor.document.text,
            chatId = NanoId.generate()
        )

        scope.launch(Dispatchers.Default) {
            PipRestClient.doPasteReview(requestBody)?.let {
                PipPasteResponseHandler(
                    pip = pip,
                    request = requestBody,
                    agentComponent = agentComponent,
                    caretOffset = offset,
                    scope = scope,
                    document = editor.document,
                    project = project,
                ).handleResponse(it)
            }
        }
    }

    override fun dispose() {
        EditorActionManager.getInstance().setActionHandler("EditorPaste", pasteHandler.originalHandler)
        agentComponent.dispose()
    }

    private suspend fun handleCodingRequest(requestBody: PipRequestBody, categorizationDto: CategorizationDto, responseHandler: PipResponseHandler) {
        pip.changeStateTo(PipState.TYPING)
        val context = getCodeContext()

        val newRequest = PipRequestBody(
            input = CodingPrompts.generateCodeRequestWithContext(requestBody.input, context),
            think = requestBody.think,
            chatId = requestBody.chatId,
            category = categorizationDto.category,
            categoryReason = categorizationDto.reason
        )

        PipRestClient.doQuestion(newRequest)?.also { response ->
            responseHandler.processCodingResponse(response, requestBody.input, requestBody.chatId)
        }
    }

    private suspend fun handleChattingRequest(requestBody: PipRequestBody, categorizationDto: CategorizationDto, responseHandler: PipResponseHandler) {
        pip.changeStateTo(PipState.THINKING)
        PipRestClient.doQuestion(
            pipRequestBody = requestBody.addCategory(categorizationDto)
        )?.also { response ->
            responseHandler.processChatResponse(response)
        }
    }

    private suspend fun handleMusicRequest(requestBody: PipRequestBody, categorizationDto: CategorizationDto, responseHandler: PipResponseHandler) {
        pip.changeStateTo(PipState.THINKING)
        PipRestClient.doQuestion(
            PipRequestBody(
                input = requestBody.input,
                think = false,
                chatId = NanoId.generate(),
                category = RequestCategory.MUSIC,
                categoryReason = categorizationDto.reason
            )
        )?.also {
            responseHandler.processMusicResponse(it)
        }
    }

    private suspend fun handleButlerRequest(requestBody: PipRequestBody, categorizationDto: CategorizationDto, responseHandler: PipResponseHandler) {
        pip.changeStateTo(PipState.THINKING)
        PipRestClient.doQuestion(
            pipRequestBody = requestBody.addCategory(categorizationDto)
        )?.also { response ->
            responseHandler.processChatResponse(response)
        }
    }

    private suspend fun handleGamingRequest(requestBody: PipRequestBody, categorizationDto: CategorizationDto, responseHandler: PipResponseHandler) {
        pip.changeStateTo(PipState.YOYO)
        PipRestClient.doQuestion(
            pipRequestBody = requestBody.addCategory(categorizationDto)
        )?.also { response ->
            responseHandler.processGamingResponse(response)
        }
    }

    private fun getCodeContext() : String {
        val javaSelection = project.getSelectedJavaDeclarations()

        val result = when {
            javaSelection.selectedText == null -> getFullDocumentContext() // Nothing selected, send whole doc as context
            javaSelection.classes.isEmpty() && javaSelection.methods.isEmpty() -> javaSelection.selectedText // No classes or methods, just send the selectec text
            javaSelection.methods.isEmpty()-> javaSelection.selectedText // No methods selected, send only selected text
            else -> getContextFromGraph(javaSelection)
        }
        return result
    }

    private fun getFullDocumentContext(): String {
        val selectedEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return ""
        return selectedEditor.document.text
    }

    private fun getContextFromGraph(javaSelection: JavaSelection): String {
        return pipSettings.state.jsonExportPath?.let { path ->
            val graphText = File(path).readText()
            val graph = GsonBuilder()
                .create()
                .fromJson(graphText, CodeGraph::class.java)

            val nodesMap = graph.nodes.associateBy { it.id }

            val relevantMethodNodeIds = ReadAction.compute<List<String>, RuntimeException> {
                graph.nodes
                    .filter { n ->
                        n.type == CodeNodeType.METHOD &&
                        javaSelection.methods.any { it.fqMethodName() == n.fqName }
                    }
                    .map { it.id }
                    .toList()
            }

            val relevantNodes = graph.links
                .asSequence()
                .filter { it.type == CodeEdgeType.INVOKES }
                .filter { it.source in relevantMethodNodeIds || it.target in relevantMethodNodeIds }
                .flatMap { listOfNotNull(nodesMap[it.source], nodesMap[it.target]) }
                .toMutableSet()

            relevantNodes.addAll(relevantMethodNodeIds.mapNotNull { nodesMap[it] })

            return if(relevantNodes.isEmpty()) javaSelection.selectedText!! else buildString {
                relevantNodes.forEach { n ->
                    appendLine("Method with name ${n.name} in class ${nodesMap[n.parentId]?.name}.java on line ${n.lineNumber ?: 0}")
                    appendLine(n.code ?: "")
                    appendLine()
                    appendLine("------")
                }
            }
        } ?: javaSelection.selectedText!!
    }

    private fun attachPasteHandler() {
        val editorActionManager = EditorActionManager.getInstance()
        val originalPasteHandler = editorActionManager.getActionHandler("EditorPaste")
        pasteHandler = PasteHandler(originalPasteHandler, this)
        editorActionManager.setActionHandler("EditorPaste", pasteHandler)
    }
}