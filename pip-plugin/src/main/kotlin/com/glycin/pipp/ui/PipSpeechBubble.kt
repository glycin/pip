package com.glycin.pipp.ui

import com.glycin.pipp.utils.SpriteSheetImageLoader
import java.awt.image.BufferedImage

private const val BASE_PATH = "/art/spritesheets"

class PipSpeechBubble() {

    fun showText(text: String) {

    }

    companion object {
        private val BUBBLE_SHOW = SpriteSheetImageLoader.loadSprites("$BASE_PATH/bubble-show.png", 64, 32, 5)
        private val BUBBLE = SpriteSheetImageLoader.loadSprites("$BASE_PATH/speech-bubble.png", 64, 32, 1).first()
    }
}