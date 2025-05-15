package com.glycin.pipp

import com.glycin.pipp.model.Pip
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.CoroutineScope
import java.awt.Rectangle

private const val FPS = 120L

class PipManager(
    private val scope: CoroutineScope,
    private val project: Project,
): Disposable {

    private val editor = FileEditorManager.getInstance(project).selectedTextEditor
    private val contentComponent = editor?.contentComponent
    private val component = editor?.component
    private val pip = Pip(Vec2(
        ((component?.width ?: 0) / 2).toFloat(),
        ((component?.height ?: 600) - 50).toFloat(),
    ), 50, 50)
    private val pipRenderer: PipRenderer = PipRenderer(pip, scope, FPS).also {
        it.bounds = contentComponent?.bounds ?: Rectangle(1200, 800)
        it.isOpaque = false
    }

    init {
        contentComponent?.let {
            it.add(pipRenderer)
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
            println(input)
        } else {
            println("cancelled")
        }
    }

    override fun dispose() {
        pipRenderer.dispose()
    }
}