package com.glycin.pipp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import java.awt.KeyboardFocusManager

@Service(Service.Level.APP)
class MainService(
    private val scope: CoroutineScope,
): Disposable {

    private var inputHandler: InputHandler? = null
    private var manager: Manager? = null

    fun init(project: Project) {
        if(manager != null) { return }
        manager = Manager(scope, project).also { pm ->
            inputHandler = InputHandler(pm).also { pi ->
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(pi)
            }
        }
    }

    override fun dispose() {
        inputHandler?.let {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(it)
        }
        inputHandler = null
        manager?.dispose()
        manager = null
    }
}