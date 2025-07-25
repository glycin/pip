package com.glycin.pipp

import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
    private val pip: Pip,
) {
    private var isThinking = false
    private var isCoding = false

    private var line = ""

    fun processSse(event: String) {
        if(isCoding) {

            if(line.trim().endsWith("``") || line.trim().endsWith("```")) {
                isCoding = false
                pip.changeStateTo(PipState.IDLE)
            }

            if(event.containsNewLine()) {
                line += event
                addToEditor(line)
                line = ""
            } else {
                line += event
            }

        } else {
            line += event
            val trimmed = line.trim()
            if(event.containsNewLine() || event == "<think>") {
                when {
                    trimmed == "<think>" || event == "<think>" ->  {
                        isThinking = true
                        pip.changeStateTo(PipState.THINKING)
                    }
                    trimmed == "</think>" || event == "</think>" -> {
                        isThinking = false
                        pip.changeStateTo(PipState.IDLE)
                    }
                    trimmed.startsWith("```") -> {
                        isCoding = true
                        pip.changeStateTo(PipState.TYPING)
                    }
                }

                line = ""
            }

            when {
                isThinking -> addToThinking(event)
                else -> addToPipText(event)
            }
        }
    }

    private fun addToThinking(event: String) {
        println("PIP IS THINKING => $event")
    }

    private fun addToPipText(event: String) {
        println("PIP IS TYPING => $event")
    }

    private fun addToEditor(event: String) {
        if(event.contains('`')) { return }
        val startOffset = editor.document.textLength
        TextWriter.writeText(startOffset, event, editor, project)
    }

    private fun String.containsNewLine() = contains("\n") || contains("\r\n") || contains("\r")
}