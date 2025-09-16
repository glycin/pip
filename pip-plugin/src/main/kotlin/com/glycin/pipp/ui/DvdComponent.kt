package com.glycin.pipp.ui

import com.glycin.pipp.Vec2
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke
import kotlin.math.roundToInt

class DvdComponent(
    private val maxX: Int,
    private val maxY: Int,
    private val scope: CoroutineScope,
    fps: Long,
    private val onExit: (DvdComponent) -> Unit,
): JComponent() {

    private val deltaTime = 1000L / fps
    private val dvdIcon = DvdIcon(
        position = Vec2(0f, 10f),
        width = 121,
        height = 54,
    )

    private var active = true
    private var xSpeed = 1.0f
    private var ySpeed = 1.0f

    init{
        dvdIcon.recolor()
        setupEscapeKeyBinding()
        moveIcon()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            g.drawImage(dvdIcon.image, dvdIcon.position.x.roundToInt(), dvdIcon.position.y.roundToInt(), dvdIcon.width, dvdIcon.height, null)
        }
    }

    private fun moveIcon() {
        scope.launch(Dispatchers.Default) {
            while(active){
                if(dvdIcon.maxXBound() >= maxX || dvdIcon.minXBound() < 0){
                    xSpeed = -xSpeed
                    dvdIcon.recolor()
                }

                if(dvdIcon.maxYBound() >= maxY || dvdIcon.minYBound() < 0) {
                    ySpeed = -ySpeed
                    dvdIcon.recolor()
                }

                dvdIcon.position += Vec2.right * xSpeed
                dvdIcon.position += Vec2.down * ySpeed
                delay(deltaTime)
            }
        }
    }

    private fun setupEscapeKeyBinding() {
        val inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW)
        val escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)

        inputMap.put(escapeStroke, "escape")
        actionMap.put("escape", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                handleEscapeKey()
            }
        })
    }

    private fun handleEscapeKey() {
        println("ESC key pressed!")
        active = false
        val inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW)
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0))
        actionMap.remove("escape")
        onExit(this)
    }
}

private class DvdIcon(
    var position: Vec2,
    val width: Int,
    val height: Int,
) {
    private val icon = PNG.DVD_PNG
    var image: BufferedImage? = icon

    fun maxXBound() = position.x + width
    fun minXBound() = position.x
    fun maxYBound() = position.y + height
    fun minYBound() = position.y

    private val colors = listOf<Color>(
        Color.RED,
        Color.WHITE,
        Color.BLACK,
        Color.BLUE,
        Color.CYAN,
        Color.ORANGE,
        Color.GREEN,
        Color.MAGENTA,
        Color.PINK,
        Color.YELLOW,
    )

    fun recolor() {
        icon?.let { i ->
            image = createColoredVersion(i, colors.random())
        }
    }

    private fun createColoredVersion(original: BufferedImage, targetColor: Color): BufferedImage {
        val coloredImage = UIUtil.createImage(
            null,
            original.width,
            original.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val targetRGB: Int = targetColor.rgb

        for (x in 0..< original.width) {
            for (y in 0..< original.height) {
                val pixel = original.getRGB(x, y)

                if (isBlack(pixel)) {
                    val alpha = (pixel shr 24) and 0xFF
                    val newRGB = (alpha shl 24) or (targetRGB and 0xFFFFFF)
                    coloredImage.setRGB(x, y, newRGB)
                } else {
                    coloredImage.setRGB(x, y, pixel)
                }
            }
        }

        return coloredImage
    }

    private fun isBlack(pixel: Int): Boolean {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        return red == 0 && green == 0 && blue == 0
    }
}