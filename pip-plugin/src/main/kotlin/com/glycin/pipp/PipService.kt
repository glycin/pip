package com.glycin.pipp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import java.awt.KeyboardFocusManager

@Service(Service.Level.APP)
class PipService(
    private val scope: CoroutineScope,
): Disposable {

    private var pipInput: PipInput? = null
    private var pipManager: PipManager? = null

    fun init(project: Project) {
        if(pipManager != null) { return }
        pipManager = PipManager(scope, project).also { pm ->
            pipInput = PipInput(pm).also { pi ->
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(pi)
            }
        }
    }

    override fun dispose() {
        pipInput?.let {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(it)
        }
        pipInput = null
        pipManager?.dispose()
        pipManager = null
    }
}