package com.glycin.pipp.ui

import com.glycin.pipp.AgentComponent
import com.glycin.pipp.Pip
import com.glycin.pipp.PipState
import com.glycin.pipp.Vec2
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.dsl.builder.*
import java.awt.Font
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent

class PipInputDialog(
    private val agentComponent: AgentComponent,
    private val pip: Pip,
    private val maxX: Float,
    private val maxY: Float,
    project: Project
): DialogWrapper(project) {

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
            separator()
            row {
                label("Pip you're drunk, go to sleep:")
                button(
                    text = "Sleep" ,
                    actionListener = {
                        agentComponent.hideSpeechBubble(true)
                        pip.position = Vec2(maxX, maxY)
                        pip.changeStateTo(PipState.SLEEPING)
                    }
                )
            }
        }
    }

    companion object {
        @JvmStatic
        val PIP_ICON: Icon = IconLoader.getIcon("/art/icons/pip.png", PipInputDialog::class.java)
    }
}