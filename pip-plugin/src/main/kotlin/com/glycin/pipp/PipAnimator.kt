package com.glycin.pipp

import com.glycin.pipp.utils.SpriteSheetImageLoader
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

private const val STANDARD_CELL_WIDTH = 40
private const val STANDARD_CELL_HEIGHT = 40
private const val BASE_PATH = "/art/spritesheets"

class PipAnimator {

    private val wallBazooka = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-bazooka.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val climb = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-climb.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val jump = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-jump.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 4)
    private val sit = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sitting.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val sleep = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sleeping.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val walk = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-walk.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val wallIdle = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-wall-grab.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val thinking = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-thinking.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 10)
    private val typing = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-typing.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8)
    private val idleLick = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sitting-licking.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 14)
    private val headbanging = SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-metal.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 5)
    private var currentSprite : BufferedImage
    private var currentAnimationIndex = 0
    private var skipFrameCount = 0

    init {
        currentSprite = sleep[0]
    }

    fun animate(state: PipState) {
        when(state) {
            PipState.IDLE -> showAnimation(idleLick)
            PipState.SLEEPING -> showAnimation(sleep)
            PipState.SITTING -> showAnimation(sit)
            PipState.WALKING -> showAnimation(walk)
            PipState.CLIMBING -> showAnimation(climb)
            PipState.HANG_IDLE -> showAnimation(wallIdle)
            PipState.WALL_SHOOTING -> showAnimation(wallBazooka)
            PipState.JUMPING -> showAnimation(jump)
            PipState.THINKING -> showAnimation(thinking)
            PipState.TYPING -> showAnimation(typing)
            PipState.METAL -> showAnimation(headbanging)
        }
    }

    fun getCurrentSprite() = currentSprite

    private fun showAnimation(sprites: List<BufferedImage>, frameDelay: Int = 12) {
        skipFrameCount++
        if(skipFrameCount % frameDelay == 0) {
            currentAnimationIndex++
        }

        if(currentAnimationIndex >= sprites.size - 1) {
            currentAnimationIndex = 0
            skipFrameCount = 0
        }
        currentSprite = sprites[currentAnimationIndex]
    }
}