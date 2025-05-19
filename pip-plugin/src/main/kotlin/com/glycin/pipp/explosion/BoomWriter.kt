package com.glycin.pipp.explosion

import com.glycin.pipp.toPoint
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import java.awt.Point
import kotlin.math.max
import kotlin.math.roundToInt

object BoomWriter {

    fun writeText(objs: List<MovableObject>, editor: Editor, project: Project, yOffset: Int) {
        val logicalPositions = objs.associateBy {
            val x = editor.xyToLogicalPosition(it.position.toPoint(yOffset = yOffset)).column
            LogicalPosition(getLogicalLineFromY(editor, it.position.y.roundToInt() + yOffset), x)
        }

        // We add an extra line/column otherwise the max character of each will get discarded :(
        val maxLine = logicalPositions.keys.maxOf { it.line } + 1
        val maxColumn = logicalPositions.keys.maxOf { it.column } + 1
        val sb = StringBuilder()

        for (line in 0 until maxLine) {
            for (column in 0 until maxColumn) {
                val foundPos = logicalPositions[LogicalPosition(line, column)]

                sb.append(foundPos?.char ?: " ")
            }
            sb.append("\n")
        }

        val s = sb.toString()

        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.replaceString(0, editor.document.textLength, s)
            }
        }
    }

    fun clear(editor: Editor, project: Project) {
        val text = editor.document.text
        val sb = StringBuilder()
        text.forEach {
            if(it == '\n' || it == '\r') { sb.append(it) }
            else sb.append(' ')
        }

        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.replaceString(0, editor.document.textLength, sb.toString())
            }
        }
    }

    // Calculate the line ourselves because the line from `editor.xyToLogicalPosition` is kinda wonky
    private fun getLogicalLineFromY(editor: Editor, y: Int): Int {
        val lineHeight = editor.lineHeight
        val visibleArea = editor.scrollingModel.visibleArea

        val firstVisibleLine = editor.xyToLogicalPosition(Point(0, visibleArea.y)).line
        val estimatedLine = firstVisibleLine + ((y - visibleArea.y) / lineHeight)

        return max(0, estimatedLine)
    }
}