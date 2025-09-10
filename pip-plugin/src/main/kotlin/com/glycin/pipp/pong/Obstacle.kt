package com.glycin.pipp.pong

import com.glycin.pipp.Vec2
import kotlin.math.abs
import kotlin.math.min

class Obstacle(
    val position: Vec2,
    val width: Int,
    val height: Int,
) {
    val minX = position.x
    val maxX = position.x + width
    val minY = position.y
    val maxY = position.y + height

    fun getCollisionNormal(colPosition: Vec2): Vec2 {
        val distLeft = abs(colPosition.x - minX)
        val distRight = abs(colPosition.x - maxX)
        val distTop = abs(colPosition.y - minY)
        val distBottom = abs(colPosition.y - maxY)

        val minDist = min(min(distLeft, distRight), min(distTop, distBottom))

        return when (minDist) {
            distLeft -> Vec2.left
            distRight -> Vec2.right
            distTop -> Vec2.up
            else -> Vec2.down
        }
    }
}