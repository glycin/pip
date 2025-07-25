package com.glycin.pipp

import com.glycin.pipp.utils.SpriteSheetImageLoader
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

private const val STANDARD_CELL_WIDTH = 40
private const val STANDARD_CELL_HEIGHT = 40
private const val BASE_PATH = "/art/spritesheets"

class PipAnimator {

    private val wallBazooka = arrayOfNulls<BufferedImage>(8)
    private val climb = arrayOfNulls<BufferedImage>(8)
    private val jump = arrayOfNulls<BufferedImage>(4)
    private val sit = arrayOfNulls<BufferedImage>(8)
    private val sleep = arrayOfNulls<BufferedImage>(8)
    private val walk = arrayOfNulls<BufferedImage>(8)
    private val wallIdle = arrayOfNulls<BufferedImage>(8)
    private val thinking = arrayOfNulls<BufferedImage>(10)
    private val typing = arrayOfNulls<BufferedImage>(8)
    private val idleLick = arrayOfNulls<BufferedImage>(14)

    private var currentSprite : BufferedImage
    private var currentAnimationIndex = 0
    private var skipFrameCount = 0

    init {
        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-bazooka.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            wallBazooka[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-climb.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            climb[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-jump.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 4).forEachIndexed { index, bufferedImage ->
            jump[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sitting.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            sit[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sleeping.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            sleep[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-walk.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            walk[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-wall-grab.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            wallIdle[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-thinking.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 10).forEachIndexed { index, bufferedImage ->
            thinking[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-typing.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 8).forEachIndexed { index, bufferedImage ->
            typing[index] = bufferedImage
        }

        SpriteSheetImageLoader.loadSprites("$BASE_PATH/pip-sitting-licking.png", STANDARD_CELL_WIDTH, STANDARD_CELL_HEIGHT, 14).forEachIndexed { index, bufferedImage ->
            idleLick[index] = bufferedImage
        }
        currentSprite = sleep[0]!!
    }

    fun animate(g: Graphics2D, state: PipState, position: Vec2, width: Int, height: Int) {
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
        }

        /*if(facing == Facing.LEFT) {
            g.drawImage(currentSprite, position.x.roundToInt() + width, position.y.roundToInt(), -width, height, null)
        }else{
            g.drawImage(currentSprite, position.x.roundToInt(), position.y.roundToInt(), width, height, null)
        }*/

        //val scaledImage = currentSprite.getScaledInstance(width, height, Image.SCALE_SMOOTH) // TODO: Pre-process the scaled images
        g.drawImage(currentSprite, position.x.roundToInt(), position.y.roundToInt(), width, height, null)
    }

    private fun showAnimation(sprites: Array<BufferedImage?>, frameDelay: Int = 12) {
        skipFrameCount++
        if(skipFrameCount % frameDelay == 0) {
            currentAnimationIndex++
        }

        if(currentAnimationIndex >= sprites.size - 1) {
            currentAnimationIndex = 0
            skipFrameCount = 0
        }
        currentSprite = sprites[currentAnimationIndex]!!
    }
}