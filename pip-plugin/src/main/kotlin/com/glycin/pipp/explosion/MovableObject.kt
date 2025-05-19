package com.glycin.pipp.explosion

import com.glycin.pipp.Vec2
import javax.swing.JLabel
import kotlin.math.*

private const val DRAG = 0.8

class MovableObject(
    var position: Vec2,
    val width: Int,
    val height: Int,
    val char: String,
    val inRange: Boolean,
    var label: JLabel? = null,
){
    fun minX() = position.x
    fun maxX() = position.x + width
    fun minY() = position.y
    fun maxY() = position.y + height
    fun midPoint() = Vec2(position.x + width / 2, position.y + height / 2)

    var force = 0.0f

    fun intersects(other: MovableObject): Boolean {
        return maxX() > other.minX() &&
                minX() < other.maxX() &&
                maxY() > other.minY() &&
                minY() < other.maxY()
    }

    fun moveWithForce(forceMagnitude: Float, explosionPos: Vec2) {
        this.force = forceMagnitude
        val angle = atan2((midPoint().y - explosionPos.y).toDouble(), (midPoint().x - explosionPos.x).toDouble())
        val deltaX = ((force * cos(angle)) * DRAG).roundToInt()
        val deltaY = ((force * sin(angle)) * DRAG).roundToInt()

        position = Vec2(max(position.x + deltaX, 0f), max(position.y + deltaY, 0f))
    }

    fun updateLabel() {
        label?.setBounds(position.x.roundToInt(), position.y.roundToInt(), width, height)
    }

    fun show() {
        label?.isVisible = true
    }

    fun rest(){
        label?.isVisible = false
    }

    override fun toString(): String {
        return "MovableObject $char at pos: $position, with width: $width and height: $height"
    }
}