package com.glycin.pipp

import com.glycin.pipp.ui.PipColors
import com.glycin.pipp.ui.PipSpeechBubble
import com.glycin.pipp.utils.Fonts
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.ui.JBColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JButton
import javax.swing.JComponent
import kotlin.math.roundToInt

class AgentComponent(
    private val pip: Pip,
    private val scope: CoroutineScope,
    private val fps : Long,
): JComponent(), Disposable {

    private val deltaTime = 1000L / fps
    private var active = true
    private var speechBubble: PipSpeechBubble? = null
    private var closeButton : JButton? = null

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
        speechBubble?.deactivate()
        speechBubble?.let {
            hideSpeechBubble()
        }
    }

    fun showSpeechBubble(message: String) {
        speechBubble?.let { remove(it) }
        closeButton?.let { remove(it) }

        speechBubble = PipSpeechBubble(
            fullText = message,
            pip = pip,
            scope = scope,
            fps = fps
        )

        closeButton = JButton("CLOSE").apply {
            isOpaque = false
            font = Fonts.pixelFont.deriveFont(Font.BOLD, 10f)
            foreground = JBColor.RED
            background = PipColors.transparent
            setBounds(pip.position.x.roundToInt(), 25, 75, 35)
            isContentAreaFilled = false

            addActionListener {
                hideSpeechBubble()
            }
        }

        add(speechBubble)
        add(closeButton)
        revalidate()
        repaint()
    }

    private fun hideSpeechBubble() {
        speechBubble?.let {
            remove(it)
            speechBubble = null
        }
        closeButton?.let {
            remove(it)
            closeButton = null
        }
        revalidate()
        repaint()
        pip.changeStateTo(PipState.IDLE)
    }

    private fun drawMovementPoints(g: Graphics) {
        g.color = JBColor.BLUE
        g.fillRect(pip.position.x.roundToInt(), pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 200, pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 400, pip.width, pip.height)
    }
}