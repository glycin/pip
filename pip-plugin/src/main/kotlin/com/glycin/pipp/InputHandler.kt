package com.glycin.pipp

import com.intellij.openapi.util.SystemInfo
import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent

class InputHandler(
    private val manager: Manager
): KeyEventDispatcher {

    private fun KeyEvent.isModifierDown(): Boolean =
        if (SystemInfo.isMac) isMetaDown && !isControlDown
        else isControlDown && !isMetaDown

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if(e.id == KeyEvent.KEY_PRESSED) {
            if(e.keyCode == KeyEvent.VK_P &&
                e.isModifierDown() &&
                !e.isAltDown &&
                !e.isShiftDown) {
                manager.showInput()
                return true
            }

            if(e.keyCode == KeyEvent.VK_L &&
                e.isModifierDown() &&
                !e.isAltDown &&
                !e.isShiftDown) {
                manager.showAndDoContextReload()
                return true
            }
        }
        return false
    }
}