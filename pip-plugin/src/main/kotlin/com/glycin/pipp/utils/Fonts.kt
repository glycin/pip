package com.glycin.pipp.utils

import java.awt.Font
import java.awt.GraphicsEnvironment

object Fonts {

    val pixelFont: Font = try {
        val fontUrl = javaClass.getResourceAsStream("/art/fonts/Pixeled.ttf")
        Font.createFont(Font.TRUETYPE_FONT, fontUrl).also {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(it)
        }
    } catch (e: Exception) {
        println("Failed to load pixel font: ${e.message}")
        Font(Font.SANS_SERIF, Font.BOLD, 16)
    }
}