package com.glycin.pipp.pong

import com.glycin.pipp.Vec2

class CollisionChecker(
    private val paddles: List<Paddle>,
    private val goals: List<Goal>,
    private val obstacles: List<Obstacle>,
) {

    fun collidesPaddles(positionToCheck: Vec2): Paddle? = paddles.firstOrNull { brick ->
        (positionToCheck.x in brick.minX()..brick.maxX()) && (positionToCheck.y in brick.minY()..brick.maxY())
    }

    fun collidesGoal(positionToCheck: Vec2): Goal? = goals.firstOrNull { goal ->
        (positionToCheck.x in goal.minX..goal.maxX) && (positionToCheck.y in goal.minY..goal.maxY)
    }

    fun collidesObstacle(positionToCheck: Vec2): Obstacle? = obstacles.firstOrNull { ob ->
        (positionToCheck.x in ob.minX..ob.maxX) && (positionToCheck.y in ob.minY..ob.maxY)
    }

    fun getBounceVector(direction: Vec2, collisionPosition: Vec2, paddle: Paddle): Vec2 {
        val colNormal = paddle.getCollisionNormal(collisionPosition)
        return when(colNormal) {
            Vec2.left, Vec2.right ->  Vec2(-direction.x, direction.y)
            Vec2.down, Vec2.up -> Vec2(direction.x, -direction.y)
            else -> direction
        }
    }

    fun getBounceVector(direction: Vec2, collisionPosition: Vec2, obstacle: Obstacle): Vec2 {
        val colNormal = obstacle.getCollisionNormal(collisionPosition)
        return when(colNormal) {
            Vec2.left, Vec2.right ->  Vec2(-direction.x, direction.y)
            Vec2.down, Vec2.up -> Vec2(direction.x, -direction.y)
            else -> direction
        }
    }
}