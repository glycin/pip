package com.glycin.pipp.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

object TextWriter {

    fun writeText(offset: Int, text: String, document: Document, project: Project) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(offset, text)
            }
        }
    }

    fun writeText(text: String, virtualFile: VirtualFile, project: Project) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                VfsUtil.saveText(virtualFile, text)
            }
        }
    }


    fun writeTextAndThen(offset: Int, text: String, document: Document, project: Project, onComplete: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(offset, text)
                onComplete()
            }
        }
    }

    fun replaceText(startOffset: Int, endOffset: Int, text: String, document: Document, project: Project) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(startOffset, endOffset, text)
            }
        }
    }

    fun deleteText(startOffset: Int, endOffset: Int, document: Document, project: Project) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                document.deleteString(startOffset, endOffset)
            }
        }
    }

    fun replaceTextAndThen(startOffset: Int, endOffset: Int, text: String, document: Document, project: Project, onComplete: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(startOffset, endOffset, text)
                onComplete()
            }
        }
    }
}