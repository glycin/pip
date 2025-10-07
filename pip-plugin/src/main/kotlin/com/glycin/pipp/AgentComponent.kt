package com.glycin.pipp

import com.glycin.pipp.ui.Gifs
import com.glycin.pipp.ui.PipColors
import com.glycin.pipp.ui.PipSpeechBubble
import com.glycin.pipp.ui.PortalAnimation
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
import javax.swing.JLabel
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
    private var catJamGif: JLabel? = null
    private var portal: PortalAnimation? = null

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
            portal?.render(g)
            //drawMovementPoints(g)
        }
    }

    override fun dispose() {
        active = false
        portal?.stop()
        portal = null
        speechBubble?.deactivate()
        speechBubble?.let {
            hideSpeechBubble()
        }
    }

    fun showSpeechBubble(message: String, setTalking: Boolean = true) {
        speechBubble?.let { remove(it) }
        closeButton?.let { remove(it) }

        if(setTalking) {
            pip.changeStateTo(PipState.TALKING)
        }

        speechBubble = PipSpeechBubble(
            fullText = message,
            pip = pip,
            scope = scope,
            fps = fps
        )

        closeButton = JButton("CLOSE").apply {
            isOpaque = false
            font = Fonts.pixelFont.deriveFont(Font.BOLD, 16f)
            foreground = JBColor.RED
            background = PipColors.transparent
            val spawnPos = pip.position + (Vec2.right * 200f) + (Vec2.up * 150f)
            setBounds(spawnPos.x.roundToInt(), spawnPos.y.roundToInt() , 75, 35)
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

    fun hideSpeechBubble(setToIdle: Boolean = true) {
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
        if(setToIdle) {
            pip.changeStateTo(PipState.IDLE)
        }
    }

    fun showCatJam() {
        catJamGif = JLabel(Gifs.CAT_JAM_GIF).apply {
            setBounds(50, 50, 112, 112)
        }
        add(catJamGif)
        revalidate()
        repaint()
    }

    fun hideCatJam() {
        if(catJamGif == null) return
        remove(catJamGif)
        catJamGif = null
        revalidate()
        repaint()
    }

    fun showPortal(x: Float, y: Float) {
        if(portal != null) {
            portal?.visible = true
        }else {
            portal = PortalAnimation(
                position = Vec2(x, y),
                scope = scope,
                fps = fps,
            )
        }
    }

    fun hidePortal() {
        portal?.visible = false
    }

    private fun drawMovementPoints(g: Graphics) {
        g.color = JBColor.BLUE
        g.fillRect(pip.position.x.roundToInt(), pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt(), pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 200, pip.width, pip.height)
        g.fillRect(pip.position.x.roundToInt() + 450, pip.position.y.roundToInt() - 400, pip.width, pip.height)
    }
}