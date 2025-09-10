package com.glycin.pipp.pong
import com.glycin.pipp.Vec2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.roundToInt

class Ball(
    var position: Vec2,
    val radius: Int = 10,
    val aiPaddle: Paddle,
    val collider: CollisionChecker,
    val service: PongGame,
    private val speed: Float = 0.25f,
    fps: Long,
    scope: CoroutineScope,
) {
    private val deltaTime = 1000L / fps
    private val immunityFrames = 10
    private var lifetime = 0
    private var direction = Vec2(1f, 1f)
    private var initialSafePosition = Vec2.zero

    private fun midPoint() = Vec2(position.x + (radius / 2), position.y + (radius / 2))

    private var active = true

    init{
        scope.launch(Dispatchers.Default) {
            while(active) {
                move(deltaTime.toFloat())
                delay(deltaTime)
            }
        }
    }

    fun stop() {
        active = false
    }

    private fun move(deltaTime: Float){
        if(lifetime <= immunityFrames) {
            lifetime++
        }else {
            collider.collidesPaddles(midPoint())?.let {
                direction = collider.getBounceVector(direction, position, it)
            } ?: collider.collidesGoal(midPoint())?.let {
                reset(it.goalIndex)
            } ?: collider.collidesObstacle(midPoint())?.let {
                direction = collider.getBounceVector(direction, position, it)
            } ?: apply {
                if(initialSafePosition == Vec2.zero) {
                    initialSafePosition = position
                }
            }
        }

        position += direction * (deltaTime * speed).roundToInt()

        if(aiPaddle.position.y <= position.y) {
            aiPaddle.moveDown(deltaTime)
        } else {
            aiPaddle.moveUp(deltaTime)
        }
    }

    private fun reset(index: Int){
        service.updateScore(index)
        position = initialSafePosition

        val rand = Random()
        val num = rand.nextInt(4)
        direction = when(num) {
            0 -> Vec2(1f, 1f)
            1 -> Vec2(-1f, 1f)
            2 -> Vec2(1f, -1f)
            3 -> Vec2(-1f, -1f)
            else -> Vec2(1f, 1f)
        }
    }
}