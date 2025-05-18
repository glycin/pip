package com.glycin.pipp

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Rectangle
import kotlin.math.roundToInt

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
            scope.launch(Dispatchers.Default) {
                delay(1500)
                println("Sit")
                pip.changeStateTo(PipState.SITTING)
                delay(2000)
                println("Walk")
                pip.changeStateTo(PipState.WALKING)
                pip.moveTo(Vec2(pip.position.x + 350, pip.position.y), 2000)
                delay(2000)
                println("Jump")
                pip.changeStateTo(PipState.JUMPING)
                pip.moveTo(Vec2(pip.position.x + 50, pip.position.y - 50), 250)
                delay(250)
                println("Climb")
                pip.changeStateTo(PipState.CLIMBING)
                pip.moveTo(Vec2(pip.position.x, pip.position.y - 200), 2000)
                delay(2000)
                println("hang")
                pip.changeStateTo(PipState.HANG_IDLE)
                delay(2000)
                println("Shoot")
                pip.changeStateTo(PipState.WALL_SHOOTING)
                delay(5000)
            }
        } else {
            println("cancelled")
        }
    }

    override fun dispose() {
        agentComponent.dispose()
    }
}