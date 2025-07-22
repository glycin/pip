package com.glycin.pipp

import com.glycin.pipp.http.CodingRequestBody
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.prompts.CodingPrompts
import com.glycin.pipp.utils.NanoId
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
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
    ), 100, 100, scope)
    private val agentComponent: AgentComponent = AgentComponent(pip, scope, FPS).also {
        it.bounds = contentComponent?.bounds ?: Rectangle(1200, 800)
        it.isOpaque = false
    }

    init {
        contentComponent?.let {
            it.add(agentComponent)
            it.revalidate()
            it.repaint()
        }
    }

    fun showInput() {
        println("doing action")
        val input = Messages.showInputDialog(
            project,
            "What don't you know this time?",
            "Ask PIP",
            Messages.getQuestionIcon(),
        )

        if(input != null) {
            val context = getContext()
            scope.launch(Dispatchers.IO) {
                val response = PipRestClient.doCodeQuestion(
                    codeRequest = CodingRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(input, context),
                        think = false,
                        chatId = NanoId.generate()
                    )
                )

                TextWriter.replaceText(0, editor!!.document.textLength, response ?: "", editor, project)
            }
        } else {
            println("cancelled")
        }
    }

    override fun dispose() {
        agentComponent.dispose()
    }

    private fun getContext() : String {
        val document = editor?.document
        return document?.text ?: ""
    }
}