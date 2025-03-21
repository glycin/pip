package com.glycin.pipp

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class InputDialogAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
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
}