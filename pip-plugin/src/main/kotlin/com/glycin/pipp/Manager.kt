package com.glycin.pipp

import com.glycin.pipp.http.CodingRequestBody
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.prompts.CodingPrompts
import com.glycin.pipp.ui.PipInputDialog
import com.glycin.pipp.utils.NanoId
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Rectangle

private const val FPS = 120L

class Manager(
    private val scope: CoroutineScope,
    private val project: Project,
): Disposable {

    private val editor = FileEditorManager.getInstance(project).selectedTextEditor
    private val contentComponent = editor?.contentComponent
    private val component = editor?.component
    private val pip = Pip(Vec2(
        ((component?.width ?: 0) / 2).toFloat(),
        ((component?.height ?: 600) - 50).toFloat(),
    ), 200, 200, scope)
    private val agentComponent: AgentComponent = AgentComponent(pip, scope, FPS).also {
        it.bounds = contentComponent?.bounds ?: Rectangle(1200, 800)
        it.isOpaque = false
    }

    private val chatIds = mutableListOf(NanoId.generate())

    init {
        contentComponent?.let {
            it.add(agentComponent)
            it.revalidate()
            it.repaint()
        }
    }

    fun showInput() {
        val dialog = PipInputDialog(project)

        if(!dialog.showAndGet() || dialog.userInput.isEmpty() || editor == null) {
            return
        }

        val context = getContext()
        val chatId = if(dialog.newChat) NanoId.generate().also { chatIds.add(it) } else chatIds.last()

        scope.launch(Dispatchers.IO) {
            if(dialog.stream) {
                val responseHandler = PipResponseHandler(editor, project)
                TextWriter.deleteText(0, editor.document.textLength, editor, project)
                PipRestClient.doCodeQuestionStream(
                    codeRequest = CodingRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(dialog.userInput, context),
                        think = dialog.think,
                        chatId =  chatId
                    )
                ).collect { e -> responseHandler.processSse(e) }
            } else {
                val response = PipRestClient.doCodeQuestion(
                    codeRequest = CodingRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(dialog.userInput, context),
                        think = dialog.think,
                        chatId =  chatId
                    )
                )

                TextWriter.replaceText(0, editor.document.textLength, response ?: "", editor, project)
            }
        }
    }

    override fun dispose() {
        agentComponent.dispose()
    }

    private fun processStreamingEvent(event: String) {
        when(event) {
            "<think>" -> { }
            "</think>" -> { }
            "```" -> {}
        }
    }

    private fun getContext() : String {
        val document = editor?.document
        return document?.text ?: ""
    }
}