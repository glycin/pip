package com.glycin.pipp

import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.settings.PipSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import java.awt.KeyboardFocusManager

@Service(Service.Level.PROJECT)
class MainService(
    private val project: Project,
    private val scope: CoroutineScope,
): Disposable {

    private var inputHandler: InputHandler? = null
    private var manager: Manager? = null

    fun init(editor: Editor, settings: PipSettings) {
        if(manager != null) { return }
        manager = Manager(scope, project, settings, editor).also { pm ->
            inputHandler = InputHandler(pm).also { pi ->
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(pi)
            }
        }
    }

    fun refocus(editor: Editor) {
        manager?.refocusPip(newEditor = editor)
    }

    override fun dispose() {
        PipRestClient.close()
        inputHandler?.let {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(it)
        }
        inputHandler = null
        manager?.dispose()
        manager = null
    }
}