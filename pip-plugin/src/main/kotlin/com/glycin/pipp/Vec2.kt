package com.glycin.pipp

import com.intellij.openapi.editor.ScrollingModel
import java.awt.Point
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Vec2(
    val x: Float = 0.0F,
    val y: Float = 0.0F,
){
    companion object {
        val zero = Vec2(0f, 0f)
        val one = Vec2(1f, 1f)
        val up = Vec2(0f, -1f)
        val down = Vec2(0f, 1f)
        val left = Vec2(-1f, 0f)
        val right = Vec2(1f, 0f)

        fun distance(a: Vec2, b: Vec2): Float {
            val dx = (b.x - a.x)
            val dy = (b.y - a.y)
            return sqrt(dx * dx + dy * dy)
        }

        fun lerp(start: Vec2, end: Vec2, t: Float): Vec2 {
            val clampedT = t.coerceIn(0f, 1f)
            return Vec2(
                start.x + (end.x - start.x) * clampedT,
                start.y + (end.y - start.y) * clampedT
            )
        }
    }

    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Int) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Int) = Vec2(x / scalar, y / scalar)

    operator fun plus(other: Float) = Vec2(x + other, y + other)
    operator fun minus(other: Float) = Vec2(x - other, y - other)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vec2(x / scalar, y / scalar)

    fun normalize(): Vec2 {
        val length = sqrt(x * x + y * y)
        if (length == 0f) return Vec2(0f, 0f)
        return Vec2(x / length, y / length)
    }

    override fun toString(): String {
        return "Vec2($x,$y)"
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Vec2) {
            abs(this.x - other.x) <= 0.0005f && abs(this.y - other.y) <= 0.0005f
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}

fun Point.toVec2() = Vec2(x.toFloat(), y.toFloat())
fun Point.toVec2(offset: ScrollingModel) = Vec2(x.toFloat() + offset.horizontalScrollOffset, y.toFloat() - offset.verticalScrollOffset)
fun Vec2.toPoint(xOffset: Int = 0, yOffset: Int = 0) = Point(x.roundToInt() + xOffset, y.roundToInt() + yOffset)