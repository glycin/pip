package com.glycin.pipp.pong

import com.glycin.pipp.Vec2

class Goal(
    var position: Vec2,
    val width: Int = 10,
    val height: Int = 60,
    val goalIndex: Int = 0,
) {
    val minX = position.x
    val maxX = position.x + width
    val minY = position.y
    val maxY = position.y + height
}