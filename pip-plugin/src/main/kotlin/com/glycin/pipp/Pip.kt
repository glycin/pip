package com.glycin.pipp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics2D
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
    private val animator = PipAnimator()

    fun changeStateTo(newState: PipState) {
        if(newState == state) { return }
        state = newState
        println("NEW PIP STATE IS $state")
    }

    fun moveTo(newPosition: Vec2, duration: Long) {
        val startTime = System.currentTimeMillis()
        val startPosition = position

        scope.launch(Dispatchers.Default) {
            while(true) {
                val elapsed = System.currentTimeMillis() - startTime
                val t = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                position = Vec2.lerp(startPosition, newPosition, t)
                if( t >= 1f) {
                    position = newPosition
                    break
                }

                delay(16)
            }
        }
    }

    fun update() {
        animator.animate(state)
    }

    fun render(g: Graphics2D) {
        g.drawImage(animator.getCurrentSprite(), position.x.roundToInt(), position.y.roundToInt(), width, height, null)
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
    TYPING
}