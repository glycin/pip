package com.glycin.pipp

import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent

class PipInput(
    private val pipManager: PipManager
): KeyEventDispatcher {

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if(e.id == KeyEvent.KEY_PRESSED) {
            if(e.keyCode == KeyEvent.VK_P &&
                e.isAltDown &&
                !e.isControlDown &&
                !e.isShiftDown) {
                pipManager.showInput()
                return true
            }
        }
        return false
    }
}