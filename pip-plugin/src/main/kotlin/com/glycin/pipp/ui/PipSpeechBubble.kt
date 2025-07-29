package com.glycin.pipp.ui

import com.glycin.pipp.Vec2
import com.glycin.pipp.utils.Extensions.toJbColor
import com.glycin.pipp.utils.SpriteSheetImageLoader
import com.intellij.openapi.application.EDT
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import kotlin.math.roundToInt

private const val BASE_PATH = "/art/spritesheets"

class PipSpeechBubble(
    private val width: Int = 192,
    private val height: Int = 96,
    fullText: String,
    pos: Vec2,
    scope: CoroutineScope,
    fps: Long,
): JComponent() {

    private var active = false
    private val deltaTime = 1000L / fps
    private val scrollPane: BubbleScrollPane

    init {
        setBounds(pos.x.roundToInt(), pos.y.roundToInt(), width, height)

        scrollPane = BubbleScrollPane(fullText, width, height, scope, deltaTime).apply {
            setBounds(0, 0, width, height)
        }

        add(scrollPane)

        revalidate()
        repaint()

        active = true
        scope.launch (Dispatchers.EDT) {
            while(active) {
                repaint()
                delay(deltaTime)
            }
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            g.drawImage(BUBBLE, 0, 0, width, height, null)
        }
    }

    fun deactivate() {
        active = false
        scrollPane.stop()
    }

    companion object {
        private val BUBBLE_SHOW = SpriteSheetImageLoader.loadSprites("$BASE_PATH/bubble-show.png", 64, 32, 5)
        private val BUBBLE = SpriteSheetImageLoader.loadSprites("$BASE_PATH/speech-bubble.png", 64, 32, 1).first()
    }
}

private class BubbleScrollPane(
    private val fullText: String,
    width: Int,
    height: Int,
    scope: CoroutineScope,
    deltaTime: Long,
): JBScrollPane() {

    private var active = false

    private val textPane: JTextPane = JTextPane().apply {
        val color = Color.BLACK.toJbColor()
        isEditable = false
        foreground = color
        val doc = styledDocument
        val style = SimpleAttributeSet().apply {
            StyleConstants.setFontFamily(this, "Dialog")
            StyleConstants.setFontSize(this, 12)
            StyleConstants.setForeground(this, color)
        }

        doc.setCharacterAttributes(0, doc.length, style, false)
    }

    init {
        setBounds(0, 0, width, height)

        isOpaque = false
        viewport.isOpaque = false
        textPane.isOpaque = false
        textPane.background = Color(0,0,0,0).toJbColor()

        setViewportView(textPane)
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(width, height)

        active = true
        scope.launch (Dispatchers.EDT) {
            var curIndex = 0
            while(curIndex < fullText.length && active) {
                textPane.text = fullText.substring(0, ++curIndex)
                textPane.caretPosition = textPane.text.length
                repaint()
                delay(deltaTime)
            }
            active = false
        }
    }

    fun stop() {
        active = false
    }
}