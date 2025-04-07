package com.glycin.pipp

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.CoroutineScope

class PipManager(
    private val scope: CoroutineScope,
    private val project: Project,
): Disposable {

    fun showInput() {
        println("doing action")
        val input = Messages.showInputDialog(
            project,
            "What?",
            "Ask PIP",
            Messages.getQuestionIcon(),
        )

        if(input != null) {
            println(input)
        } else {
            println("cancelled")
        }
    }

    override fun dispose() {
        //TODO: Cleanup
    }
}