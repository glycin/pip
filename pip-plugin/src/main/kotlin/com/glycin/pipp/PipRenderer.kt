package com.glycin.pipp

import com.glycin.pipp.model.Pip
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

class PipRenderer(
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
            drawPip(g)
        }
    }

    override fun dispose() {
        active = false
    }

    private fun drawPip(g: Graphics) {
        g.color = JBColor.RED
        g.fillOval(pip.position.x.roundToInt(), pip.position.y.roundToInt(), pip.width, pip.height)
    }
}