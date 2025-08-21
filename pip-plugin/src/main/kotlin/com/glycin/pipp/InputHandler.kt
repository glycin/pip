package com.glycin.pipp

import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent

class InputHandler(
    private val manager: Manager
): KeyEventDispatcher {

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if(e.id == KeyEvent.KEY_PRESSED) {
            if(e.keyCode == KeyEvent.VK_P &&
                e.isAltDown &&
                !e.isControlDown &&
                !e.isShiftDown) {
                manager.showInput()
                return true
            }

            if(e.keyCode == KeyEvent.VK_L &&
                e.isAltDown &&
                !e.isControlDown &&
                !e.isShiftDown) {
                manager.showAndDoContextReload()
                return true
            }
        }
        return false
    }
}