package com.glycin.pipp.ui

import com.glycin.pipp.Pip
import com.glycin.pipp.Vec2
import com.glycin.pipp.utils.Extensions.toJbColor
import com.glycin.pipp.utils.Fonts
import com.glycin.pipp.utils.SpriteSheetImageLoader
import com.intellij.openapi.application.EDT
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import kotlin.math.roundToInt

private const val BASE_PATH = "/art/spritesheets"

private const val SCROLL_X_PADDING = 5
private const val SCROLL_Y_PADDING = 3
private const val SCROLL_BOTTOM_PADDING = 35
class PipSpeechBubble(
    private val width: Int = 384,
    private val height: Int = 192,
    private val pip: Pip,
    fullText: String,
    scope: CoroutineScope,
    fps: Long,
): JComponent() {

    private var active = false
    private val deltaTime = 1000L / fps
    private val scrollPane: BubbleScrollPane

    init {
        val pos = pip.position + (Vec2.up * 100f) + (Vec2.left * 180)
        setBounds(pos.x.roundToInt(), pos.y.roundToInt(), width, height)

        scrollPane = BubbleScrollPane(fullText, scope, deltaTime).apply {
            setBounds(SCROLL_X_PADDING, SCROLL_Y_PADDING, width - SCROLL_X_PADDING, height - SCROLL_BOTTOM_PADDING)
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
        val pos = pip.position + (Vec2.up * 100f) + (Vec2.left * 180)
        setBounds(pos.x.roundToInt(), pos.y.roundToInt(), width, height)
        scrollPane.setBounds(SCROLL_X_PADDING, SCROLL_Y_PADDING, width - SCROLL_X_PADDING, height - SCROLL_BOTTOM_PADDING)
        if(g is Graphics2D) {
            g.drawImage(BUBBLE, 0, 0, width, height, null)
        }
    }

    fun deactivate() {
        active = false
        scrollPane.stop()
    }

    companion object {
        //private val BUBBLE_SHOW = SpriteSheetImageLoader.loadSprites("$BASE_PATH/bubble-show.png", 64, 32, 5)
        private val BUBBLE = SpriteSheetImageLoader.loadSprites("$BASE_PATH/speech-bubble.png", 64, 32, 1).first()
    }
}

private class BubbleScrollPane(
    private val fullText: String,
    scope: CoroutineScope,
    deltaTime: Long,
): JBScrollPane() {

    private var active = false

    private val textPane: JTextPane = JTextPane().apply {
        val color = Color.BLACK.toJbColor()
        isEditable = false
        foreground = color
        margin = Insets(10, 15, 30, 15)
        val doc = styledDocument
        val font = Fonts.pixelFont
        val style = SimpleAttributeSet().apply {
            StyleConstants.setFontFamily(inputAttributes, font.family)
            StyleConstants.setFontSize(inputAttributes, 20)
            StyleConstants.setItalic(inputAttributes, (font.style and Font.ITALIC) != 0)
            StyleConstants.setBold(inputAttributes, (font.style and Font.BOLD) != 0)
            StyleConstants.setForeground(inputAttributes, color)
        }

        doc.setCharacterAttributes(0, doc.length, style, false)
    }

    init {
        isOpaque = false
        viewport.isOpaque = false
        textPane.isOpaque = false
        textPane.background = PipColors.transparent
        setViewportView(textPane)
        viewportBorder = JBUI.Borders.empty(0, 0, 15, 0)
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER

        active = true
        scope.launch (Dispatchers.EDT) {
            var curIndex = 0
            while(curIndex < fullText.length && active) {
                textPane.text = fullText.take(++curIndex)
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