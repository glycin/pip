package com.glycin.pipp

import com.glycin.pipp.context.CodeGraphBuilder
import com.glycin.pipp.http.PipRequestBody
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.prompts.CodingPrompts
import com.glycin.pipp.ui.PipInputDialog
import com.glycin.pipp.utils.NanoId
import com.glycin.pipp.utils.TextWriter
import com.google.gson.GsonBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JComponent

private const val FPS = 120L

class Manager(
    private val scope: CoroutineScope,
    private val project: Project,
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

    init {
        contentComponent.let {
            it.add(agentComponent)
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
            scrollModel.addVisibleAreaListener {
                //TODO: Animate the movement when screen resizes
                val newX = (it.newRectangle.width - pip.width - 5f) + it.newRectangle.x
                val newY = (it.newRectangle.height - pip.height + 35f) + it.newRectangle.y
                pip.position = Vec2(newX, newY)
            }
            pip.position = Vec2(visibleArea.width - pip.width - 5f, visibleArea.height - pip.height.toFloat() + 35)
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
                val responseHandler = PipStreamResponseHandler(editor, project, pip)
                TextWriter.deleteText(0, editor.document.textLength, editor, project)
                PipRestClient.doCodeQuestionStream(
                    pipRequest = PipRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(dialog.userInput, context),
                        think = dialog.think,
                        chatId =  chatId
                    )
                ).collect { e -> responseHandler.processSse(e) }
            } else {
                val responseHandler = PipResponseHandler(editor, project, scope, pip, agentComponent)
                pip.changeStateTo(PipState.THINKING)
                PipRestClient.doQuestion(
                    pipRequestBody = PipRequestBody(
                        input = CodingPrompts.generateCodeRequestWithContext(dialog.userInput, context),
                        think = dialog.think,
                        chatId =  chatId
                    )
                )?.also {
                    responseHandler.processResponse(it)
                }
            }
        }
    }

    fun showContextReload() {
        DumbService.getInstance(project).runReadActionInSmartMode {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    val (nodes, edges) = CodeGraphBuilder(project).build()
                    val json = GsonBuilder().disableHtmlEscaping().create()
                        .toJson(mapOf("nodes" to nodes, "links" to edges))
                    println(json)
                },
                "Building PSI Graph",
                true,
                project
            )
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