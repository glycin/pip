package com.glycin.pipp.pong

import com.intellij.openapi.application.EDT
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import kotlin.math.roundToInt

class PongComponent(
    private val obstacles: MutableList<Obstacle>,
    private val ball: Ball,
    private val p1Paddle: Paddle,
    private val p2Paddle: Paddle,
    private val scope: CoroutineScope,
    fps : Long,
): JComponent() {

    private val deltaTime = 1000L / fps
    private var active = true

    fun start() {
        isFocusable = true

        scope.launch (Dispatchers.EDT) {
            while(active) {
                repaint()
                delay(deltaTime)
            }
        }
    }

    fun stop() {
        active = false
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if(g is Graphics2D) {
            //drawObstacles(g)
            drawPlayers(g)
            drawBall(g)
        }
    }

    private fun drawBall(g: Graphics2D) {
        g.color = JBColor.RED
        g.fillOval(ball.position.x.roundToInt(), ball.position.y.roundToInt(), ball.radius, ball.radius)
    }

    private fun drawObstacles(g: Graphics2D) {
        obstacles.forEach { obstacle ->
            g.color = Gray._255
            g.drawRect(obstacle.position.x.roundToInt(), obstacle.position.y.roundToInt(), obstacle.width, obstacle.height)
        }
    }

    private fun drawPlayers(g: Graphics2D) {
        g.color = JBColor.BLUE
        g.fillRect(p1Paddle.position.x.roundToInt(), p1Paddle.position.y.roundToInt(), p1Paddle.width, p1Paddle.height)

        g.color = JBColor.GREEN
        g.fillRect(p2Paddle.position.x.roundToInt(), p2Paddle.position.y.roundToInt(), p2Paddle.width, p2Paddle.height)
    }
}