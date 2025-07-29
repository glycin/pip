package com.glycin.pipp.ui

import com.glycin.pipp.utils.SpriteSheetImageLoader
import com.intellij.ui.JBColor
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.Timer
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

private const val BASE_PATH = "/art/spritesheets"

class PipSpeechBubble(
    private val fullText: String
): JScrollPane() {

    private val textPane: JTextPane = JTextPane().apply {
        isEditable = false

        val doc = styledDocument
        val style = SimpleAttributeSet().apply {
            StyleConstants.setFontFamily(this, "Dialog")
            StyleConstants.setFontSize(this, 12)
            StyleConstants.setForeground(this, JBColor.WHITE)
        }

        doc.setCharacterAttributes(0, doc.length, style, false)
    }

    private var currentIndex = 0
    private val timer = Timer(50) { // 50ms delay between characters
        if (currentIndex < fullText.length) {
            textPane.text = fullText.substring(0, ++currentIndex)
            // Auto-scroll to bottom as text appears
            textPane.caretPosition = textPane.text.length
        } else {
            (it.source as Timer).stop()
        }
    }

    init {
        setViewportView(textPane)
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(250, 100)

        startTypewriter()
    }

    fun startTypewriter() {
        currentIndex = 0
        textPane.text = ""
        timer.start()
    }

    fun stopTypewriter() {
        timer.stop()
    }

    companion object {
        private val BUBBLE_SHOW = SpriteSheetImageLoader.loadSprites("$BASE_PATH/bubble-show.png", 64, 32, 5)
        private val BUBBLE = SpriteSheetImageLoader.loadSprites("$BASE_PATH/speech-bubble.png", 64, 32, 1).first()
    }
}