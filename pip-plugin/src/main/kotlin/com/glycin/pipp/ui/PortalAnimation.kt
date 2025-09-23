package com.glycin.pipp.ui

import com.glycin.pipp.Vec2
import com.glycin.pipp.utils.SpriteSheetImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class PortalAnimation(
    var position: Vec2,
    scope: CoroutineScope,
    fps: Long,
) {
    private val animation = SpriteSheetImageLoader.loadSprites("/art/spritesheets/teleport.png", 40,40,4)
    private val deltaTime = 1000L / fps

    private var currentSprite : BufferedImage
    private var currentAnimationIndex = 0
    private var skipFrameCount = 0

    private var active = true
    var visible = true

    init {
        currentSprite = animation.first()
        scope.launch(Dispatchers.Default) {
            while(active){
                animate()
                delay(deltaTime)
            }
        }
    }

    fun render(g: Graphics2D) {
        if(visible) {
            g.drawImage(currentSprite, position.x.roundToInt(), position.y.roundToInt(), 160, 160, null)
        }
    }

    fun stop() {
        active = false
    }

    private fun animate() {
        skipFrameCount++
        if(skipFrameCount % 12 == 0) {
            currentAnimationIndex++
        }

        if(currentAnimationIndex >= animation.size - 1) {
            currentAnimationIndex = 0
            skipFrameCount = 0
        }
        currentSprite = animation[currentAnimationIndex]
    }
}