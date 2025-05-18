package com.glycin.pipp.utils

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object SpriteSheetImageLoader{

    fun loadSprites(
        spriteSheetPath: String,
        cellWidth: Int,
        cellHeight: Int,
        numSprites: Int,
    ): List<BufferedImage> {
        val spriteSheet: BufferedImage = ImageIO.read(this.javaClass.getResource(spriteSheetPath))
        val sprites = mutableListOf<BufferedImage>()
        val columns = spriteSheet.width / cellWidth
        for (index in 0 until numSprites) {
            val x = (index % columns) * cellWidth
            val y = (index / columns) * cellHeight
            if (x + cellWidth <= spriteSheet.width && y + cellHeight <= spriteSheet.height) {
                sprites.add(spriteSheet.getSubimage(x, y, cellWidth, cellHeight))
            }
        }
        return sprites
    }
}