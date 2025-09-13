package com.glycin.pipp.tictactoe

import com.glycin.pipp.AgentComponent
import com.glycin.pipp.Pip
import com.intellij.ui.components.CheckBox
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JComponent
import javax.swing.BoxLayout
import javax.swing.Box

class TicTacToeStarter(
    private val scope: CoroutineScope,
    private val chatId: String,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
) {
    fun showTicTacToe() {
        SwingUtilities.invokeLater {
            JFrame("Tic Tac Toe").apply {
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

                layout = BorderLayout()
                add(game, BorderLayout.CENTER)
                add(southPanel, BorderLayout.SOUTH)

                defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                pack()
                setLocationRelativeTo(null)
                isVisible = true
            }
        }
    }
}