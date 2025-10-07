package com.glycin.pipp.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.dsl.builder.*
import java.awt.Font
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent

class PipInputDialog(project: Project): DialogWrapper(project) {

    var userInput: String = ""
    var newChat: Boolean = true
    var think: Boolean = false
    var stream: Boolean = false

    init {
        title = "Ask PIP"
        setOKButtonText("")
        setOKButtonIcon(PIP_ICON)
        setCancelButtonText("Cancel")
        window.setIconImage((PIP_ICON as? ImageIcon)?.image)
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label(PipStandardResponses.randomTitle)
                    .applyToComponent {
                        font = font.deriveFont(Font.BOLD)
                    }
            }
            separator()
            row {
                textArea()
                    .label("Don't waste my time...")
                    .bindText(::userInput)
                    .focused()
                    .columns(25)
                    .rows(5)
                    .align(AlignX.FILL)
            }
            separator()
            row {
                checkBox("Start new chat?")
                    .bindSelected(::newChat)
            }
            separator()
            row {
                checkBox("Think?")
                    .bindSelected(::think)
            }
            separator()
            row {
                checkBox("Stream?")
                    .bindSelected(::stream)
            }
        }
    }

    companion object {
        @JvmStatic
        val PIP_ICON: Icon = IconLoader.getIcon("/art/icons/pip.png", PipInputDialog::class.java)
    }
}