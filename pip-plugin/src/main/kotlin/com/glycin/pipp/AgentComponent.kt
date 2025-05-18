package com.glycin.pipp

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.ui.JBColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import kotlin.math.roundToInt

class AgentComponent(
    private val pip: Pip,
    scope: CoroutineScope,
    fps : Long,
): JComponent(), Disposable {

    private val deltaTime = 1000L / fps
    private var active = true

    init {
        scope.launch(Dispatchers.EDT) {
            while (active) {
                repaint()
                delay(deltaTime)
            }
        }
    }

    //TODO: This is also called when intellij forces a repaint. Detach it from the intellij loop by having a dedicated movement update loop for Pip
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            pip.render(g)
            //drawMovementPoints(g)
        }
    }

    override fun dispose() {
        active = false
    }

    private fun drawMovementPoints(g: Graphics) {
        g.color = JBColor.BLUE
        g.fillRect(pip.position.x.roundToInt(), pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 200, pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 400, pip.width, pip.height)
    }
}