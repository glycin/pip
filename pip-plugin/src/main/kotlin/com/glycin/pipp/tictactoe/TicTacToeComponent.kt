package com.glycin.pipp.tictactoe

import com.glycin.pipp.AgentComponent
import com.glycin.pipp.Pip
import com.glycin.pipp.PipState
import com.glycin.pipp.http.PipRestClient
import com.glycin.pipp.http.TicTacToeRequestBody
import com.intellij.ui.JBColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.*
import javax.swing.*

//Like, totally not AI generated
class TicTacToeComponent(
    private val scope: CoroutineScope,
    private val chatId: String,
    private val pip: Pip,
    private val pipAgent: AgentComponent,
) : JPanel() {
    private lateinit var buttons: Array<Array<JButton>>
    private var gameOver = false
    private var currentPlayer = "X"
    private var think = true

    private val playerMoves = mutableListOf<Int>()
    private val aiMoves = mutableListOf<Int>()
    init {
        initializeComponent()
        println("INITIALIZED")
    }

    private fun initializeComponent() {
        layout = GridLayout(3, 3, 2, 2)
        preferredSize = Dimension(300, 300)
        background = JBColor.BLACK
        isOpaque = false
        buttons = Array(3) { Array(3) { JButton() } }
        createButtons()
    }

    private fun createButtons() {
        for (row in 0..2) {
            for (col in 0..2) {
                val cellNumber = row * 3 + col + 1 // Calculate cell number (1-9)

                val button = JButton().apply {
                    font = Font("Arial", Font.BOLD, 48)
                    isFocusable = false
                    background = JBColor.WHITE.brighter()
                    border = BorderFactory.createRaisedBevelBorder()
                    addActionListener { handleCellClick(this, cellNumber) }
                }

                buttons[row][col] = button
                add(button)
            }
        }
    }

    private fun handleCellClick(clickedButton: JButton, cellNumber: Int) {
        if (gameOver || clickedButton.text.isNotEmpty()) return
        println("Clicked $cellNumber")
        playerMoves.add(cellNumber)
        // Make the move
        clickedButton.text = currentPlayer
        clickedButton.isEnabled = false

        // Check for win or tie
        when {
            checkWinner(currentPlayer) -> {
                gameOver = true
                showGameResult("$currentPlayer wins!")
                highlightWinningCells()
            }
            isBoardFull() -> {
                gameOver = true
                showGameResult("It's a tie!")
            }
            else -> {
                currentPlayer = "X"
                doAiMove()
            }
        }
    }

    private fun doAiMove() {
        val body = TicTacToeRequestBody(
            playerMoves = playerMoves.joinToString(","),
            aiMoves = aiMoves.joinToString(","),
            think = think,
            chatId = chatId
        )
        scope.launch(Dispatchers.Default) {
            PipRestClient.doTicTacToe(body)?.let { response ->
                println("AI RESPONDED WITH ${response.move}")
                aiMoves.add(response.move)
                val (row, col) = getRowColFromCellNumber(response.move)
                val button = buttons[row][col]
                button.text = "O"
                button.isEnabled = false
                pipAgent.showSpeechBubble(response.response)
                delay(15000)
                pipAgent.hideSpeechBubble(false)
                pip.changeStateTo(PipState.YOYO)
            }
        }
    }

    private fun checkWinner(player: String): Boolean {
        // Check rows
        for (row in 0..2) {
            if (buttons[row].all { it.text == player }) return true
        }

        // Check columns
        for (col in 0..2) {
            if ((0..2).all { buttons[it][col].text == player }) return true
        }

        // Check diagonals
        if ((0..2).all { buttons[it][it].text == player }) return true
        if ((0..2).all { buttons[it][2 - it].text == player }) return true

        return false
    }

    private fun isBoardFull(): Boolean {
        return buttons.all { row -> row.all { it.text.isNotEmpty() } }
    }

    private fun highlightWinningCells() {
        val winner = currentPlayer

        // Highlight winning rows
        for (row in 0..2) {
            if (buttons[row].all { it.text == winner }) {
                buttons[row].forEach { it.background = JBColor.GREEN }
            }
        }

        // Highlight winning columns
        for (col in 0..2) {
            if ((0..2).all { buttons[it][col].text == winner }) {
                (0..2).forEach { buttons[it][col].background = JBColor.GREEN }
            }
        }

        // Highlight winning diagonals
        if ((0..2).all { buttons[it][it].text == winner }) {
            (0..2).forEach { buttons[it][it].background = JBColor.GREEN }
        }

        if ((0..2).all { buttons[it][2 - it].text == winner }) {
            (0..2).forEach { buttons[it][2 - it].background = JBColor.GREEN }
        }
    }

    private fun showGameResult(message: String) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE)
    }

    fun resetGame() {
        gameOver = false
        currentPlayer = "X"
        playerMoves.clear()
        aiMoves.clear()
        buttons.forEach { row ->
            row.forEach { button ->
                button.text = ""
                button.isEnabled = true
                button.background = JBColor.WHITE.brighter()
            }
        }
    }

    fun changeThink(thinking: Boolean) {
        think = thinking
    }

    fun getCurrentPlayer(): String = currentPlayer
    fun isGameOver(): Boolean = gameOver

    private fun getRowColFromCellNumber(cellNumber: Int): Pair<Int, Int> {
        val adjustedCell = cellNumber - 1  // Convert to 0-based indexing
        val row = adjustedCell / 3         // Integer division
        val col = adjustedCell % 3         // Modulo operation
        return Pair(row, col)
    }
}
