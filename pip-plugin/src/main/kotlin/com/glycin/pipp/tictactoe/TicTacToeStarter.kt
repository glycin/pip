package com.glycin.pipp.tictactoe

import com.glycin.pipp.AgentComponent
import com.glycin.pipp.Pip
import com.intellij.openapi.project.Project
import com.intellij.ui.components.CheckBox
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import javax.swing.SwingUtilities
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JComponent
import javax.swing.BoxLayout
import javax.swing.Box
import com.intellij.openapi.ui.popup.JBPopupFactory

class TicTacToeStarter(
    private val project: Project?,
    private val scope: CoroutineScope,
    private val chatId: String,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
) {
    fun showTicTacToe() {
        SwingUtilities.invokeLater {

            val game = TicTacToeComponent(
                scope = scope,
                chatId = chatId,
                pip = pip,
                pipAgent = agentComponent
            )

            val southPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                // Reset button
                val resetButton = JButton("Reset Game").apply {
                    addActionListener { game.resetGame() }
                    alignmentX = JComponent.CENTER_ALIGNMENT
                }

                // Checkbox
                val aiModeCheckbox = CheckBox("Think Pip, think").apply {
                    alignmentX = JComponent.CENTER_ALIGNMENT
                    addActionListener { e ->
                        val isSelected = (e.source as JCheckBox).isSelected
                        game.changeThink(isSelected)
                    }
                }

                add(resetButton)
                add(Box.createVerticalStrut(5)) // 5px spacing
                add(aiModeCheckbox)
            }

            // Root panel for popup content
            val root = JPanel(BorderLayout()).apply {
                add(game, BorderLayout.CENTER)
                add(southPanel, BorderLayout.SOUTH)
            }

            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(root, null)
                .setTitle("Tic Tac Toe")
                .setResizable(true)
                .setShowBorder(true)
                .setMovable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)   // keep open on outside clicks
                .setCancelOnOtherWindowOpen(false)// keep open when other windows appear
                .setCancelKeyEnabled(true)
                .createPopup()

            popup.showInFocusCenter()
        }
    }
}