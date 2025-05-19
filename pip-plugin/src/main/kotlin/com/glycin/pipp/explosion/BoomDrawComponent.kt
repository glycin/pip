package com.glycin.pipp.explosion

import com.glycin.pipp.Vec2
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JLabel
import kotlin.math.*

class BoomDrawComponent(
    private val explosionImages: Array<BufferedImage?>,
    private val explosionObjects: List<MovableObject>,
    private val position: Vec2,
    private val width: Int = 250,
    private val height: Int = 250,
    private val finishedCallback: (BoomDrawComponent) -> Unit,
    scope: CoroutineScope,
    fps: Long,
): JComponent() {
    private val deltaTime = 1000L / fps

    private val explosionDecay = 50
    private val explosionForce = 10
    private val explosionRadius = 200

    private var currentAnimationIndex = 0
    private var skipFrameCount = 0
    private var currentSprite : BufferedImage? = explosionImages[0]

    private var active = true

    init {
        createLabels()

        scope.launch(Dispatchers.Default) {
            while (active) {
                showAnimation(4)
                repaint()
                delay(deltaTime)
            }
        }

        explode(position, scope)
    }

    fun cleanup() {
        currentSprite = null
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            val pos = Vec2(position.x - (width / 2), position.y - (height / 2))
            g.drawImage(currentSprite, pos.x.roundToInt(), pos.y.roundToInt(), width, height, null)
            //drawObjects(g)
        }
    }

    private fun showAnimation(frameDelay: Int) {
        skipFrameCount++
        if(skipFrameCount % frameDelay == 0) {
            currentAnimationIndex++
        }

        if(currentAnimationIndex >= explosionImages.size - 1) {
            currentAnimationIndex = 0
            skipFrameCount = 0
            active = false
            finishedCallback.invoke(this)
        }
        currentSprite = explosionImages[currentAnimationIndex]!!
    }

    private fun createLabels(){
        val scheme = EditorColorsManager.getInstance().globalScheme
        val fontPreferences = scheme.fontPreferences

        explosionObjects.forEach { eo ->
            val objLabel = JLabel(eo.char)
            objLabel.font = Font(fontPreferences.fontFamily, 0, fontPreferences.getSize(fontPreferences.fontFamily))
            objLabel.setBounds(eo.position.x.roundToInt(), eo.position.y.roundToInt(), eo.width, eo.height)
            objLabel.isVisible = false
            add(objLabel)
            eo.label = objLabel
            eo.show()
        }
        repaint()
    }

    private fun explode(explosionPos: Vec2, scope: CoroutineScope) {
        val start = System.currentTimeMillis()
        val duration = 1000 //millis
        val endTime = start + duration

        scope.launch (Dispatchers.Default) {
            delay(50) // A little delay to make the effect match the gif
            while(System.currentTimeMillis() < endTime) {
                explosionObjects.filter {
                    it.inRange
                }.onEach { mo ->
                    val centerPos = mo.midPoint()
                    val distance = Vec2.distance(centerPos, explosionPos)

                    if(distance < explosionRadius) {
                        val forceMagnitude = explosionForce * (explosionRadius - distance) / explosionDecay
                        mo.moveWithForce(forceMagnitude, explosionPos)
                    }else {
                        mo.force = 0.0f
                    }

                    handleCollisions(mo)
                }
                delay(deltaTime)
            }
            explosionObjects.forEach { it.rest() }
        }
    }

    // Fake it till you make it physics...
    private fun handleCollisions(a: MovableObject) {
        val midPointA = a.midPoint()
        explosionObjects
            .filter {
                it.intersects(a)
            }
            .onEach { b ->
                val midPointB = b.midPoint()
                val angle = atan2((midPointB.y - midPointA.y).toDouble(), (midPointB.x - midPointA.x).toDouble())

                val overlapX = a.maxX() - b.minX()
                val overlapY = a.maxY() - b.minY()
                val moveDistance = min(overlapX, overlapY)  / 2

                val moveX = (moveDistance * cos(angle)).toInt()
                val moveY = (moveDistance * sin(angle)).toInt()

                a.position -= Vec2(moveX.toFloat(), moveY.toFloat())
                b.position += Vec2(moveX.toFloat(), moveY.toFloat())

                a.updateLabel()
                b.updateLabel()
            }
    }

    // Used for debugging
    private fun drawObjects(g: Graphics2D) {
        explosionObjects.forEach { eo ->
            if(eo.inRange){
                g.color = JBColor.GREEN
            } else {
                g.color = Gray._117
            }
            g.drawRect(eo.position.x.roundToInt(), eo.position.y.roundToInt(), eo.width, eo.height)
        }
    }
}