package com.glycin.pipp

import com.glycin.pipp.ui.PipSpeechBubble
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
    private var speechBubble: PipSpeechBubble? = null

    init {
        scope.launch(Dispatchers.EDT) {
            while (active) {
                pip.update()
                repaint()
                delay(deltaTime)
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            pip.render(g)
            //drawMovementPoints(g)
        }
    }

    override fun dispose() {
        active = false
        speechBubble?.stopTypewriter()
        speechBubble?.let {
            hideSpeechBubble()
        }
    }

    fun showSpeechBubble(message: String) {
        speechBubble?.let { remove(it) }

        speechBubble = PipSpeechBubble(message).apply {
            setBounds(50, 20, 250, 100) // Position the bubble
        }

        add(speechBubble)
        revalidate()
        repaint()
    }

    fun hideSpeechBubble() {
        speechBubble?.let {
            remove(it)
            speechBubble = null
        }
        revalidate()
        repaint()
    }

    private fun drawMovementPoints(g: Graphics) {
        g.color = JBColor.BLUE
        g.fillRect(pip.position.x.roundToInt(), pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 200, pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 400, pip.width, pip.height)
    }
}