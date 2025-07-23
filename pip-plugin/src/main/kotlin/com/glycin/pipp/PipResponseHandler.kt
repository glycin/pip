package com.glycin.pipp

import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
) {
    private var isThinking = false
    private var isCoding = false

    private var line = ""

    fun processSse(event: String) {
        if(isCoding) {

            if(line.trim().endsWith("``")) {
                isCoding = false
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
                    trimmed == "<think>" || event == "<think>" -> isThinking = true
                    trimmed == "</think>" || event == "</think>" -> isThinking = false
                    trimmed.startsWith("```") -> isCoding = true
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