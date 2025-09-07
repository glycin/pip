package com.glycin.pipp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.math.roundToInt

private const val DEFAULT_WIDTH = 200
private const val DEFAULT_HEIGHT = 200

class Pip(
    var position: Vec2,
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    private val scope: CoroutineScope,
) {
    private var state = PipState.SLEEPING
    private var facing = Facing.LEFT
    private val animator = PipAnimator()

    fun changeStateTo(newState: PipState) {
        if(newState == state) { return }
        state = newState
        println("NEW PIP STATE IS $state")
    }

    fun moveTo(newPosition: Vec2, duration: Long, movingAnimationState: PipState = PipState.WALKING, endAnimationState: PipState = PipState.IDLE) {
        if(Vec2.distance(newPosition, position) < 1.0f) return
        changeStateTo(movingAnimationState)
        val startTime = System.currentTimeMillis()
        val startPosition = position

        facing = if(newPosition.x < position.x) Facing.LEFT else Facing.RIGHT

        scope.launch(Dispatchers.Default) {
            while(true) {
                val elapsed = System.currentTimeMillis() - startTime
                val t = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                position = Vec2.lerp(startPosition, newPosition, t)
                if( t >= 1f) {
                    position = newPosition
                    changeStateTo(endAnimationState)
                    break
                }

                delay(16)
            }
        }
    }

    fun face(newFacing: Facing) {
        if(newFacing != facing) {
            facing = newFacing
        }
    }

    fun update() {
        animator.animate(state)
    }

    fun render(g: Graphics2D) {
        val currentSprite = animator.getCurrentSprite()

        if(facing == Facing.LEFT) {
            g.drawImage(currentSprite, position.x.roundToInt() + width, position.y.roundToInt(), -width, height, null)
        }else{
            g.drawImage(currentSprite, position.x.roundToInt(), position.y.roundToInt(), width, height, null)
        }
    }

    fun anchor(maxX: Float, maxY: Float) {
        when(state) {
            PipState.CLIMBING, PipState.HANG_IDLE, PipState.WALL_SHOOTING -> return
            else -> {
                val x = position.x.coerceIn(0f, max(maxX, 1f))
                position = Vec2(x, maxY)
            }
        }
    }
}

enum class PipState {
    IDLE,
    SLEEPING,
    SITTING,
    WALKING,
    CLIMBING,
    HANG_IDLE,
    WALL_SHOOTING,
    JUMPING,
    THINKING,
    TYPING,
    METAL,
    MAGIC,
    DEAL_WITH_IT,
    TALKING,
    STOP,
    YOYO,
    JUMP_UP,
    JUMP_DOWN,
}

enum class Facing {
    LEFT,
    RIGHT
}