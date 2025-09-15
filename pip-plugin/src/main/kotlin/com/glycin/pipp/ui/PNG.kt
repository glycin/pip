package com.glycin.pipp.ui

import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

object PNG {

    val DVD_PNG : BufferedImage? by lazy {
        try {
            val url = Gifs::class.java.getResource("/art/png/dvd.png")
            url?.let { ImageIO.read(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}