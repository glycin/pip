package com.glycin.pipp.pong

import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent

class PongInput(
    private val p1: Paddle,
    fps: Long,
    private val onEscape: () -> Unit = {},
): KeyEventDispatcher {

    private val deltaTime = 1000.0f / fps

    override fun dispatchKeyEvent(e: KeyEvent?): Boolean {

        if (e?.id == KeyEvent.KEY_PRESSED) {
            when (e.keyCode) {
                KeyEvent.VK_W -> {
                    p1.moveUp(deltaTime)
                }
                KeyEvent.VK_S -> {
                    p1.moveDown(deltaTime)
                }

                KeyEvent.VK_ESCAPE -> {
                    onEscape()
                }
            }
        }
        // Returning false allows the event to be redispatched to the target component
        // Returning true consumes the event, preventing it from being redispatched
        return true
    }
}