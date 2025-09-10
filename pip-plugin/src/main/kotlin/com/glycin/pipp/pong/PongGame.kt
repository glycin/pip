package com.glycin.pipp.pong

import com.glycin.pipp.Vec2
import com.glycin.pipp.utils.Extensions.toVec2
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.KeyboardFocusManager
import kotlin.math.roundToInt

private const val FPS = 120L

class PongGame(
    private val project: Project,
    private val editor: Editor,
    private val scope: CoroutineScope,
    private val onEnd: () -> Unit = {}
) {

    private var score1 = 0
    private var score2 = 0

    private var textLine: Int = 0
    private lateinit var openDocument: Document
    private lateinit var openProject: Project
    private lateinit var openEditor: Editor
    private lateinit var pongComponent: PongComponent
    private lateinit var input: PongInput
    private lateinit var ball: Ball

    fun initGame() {
        openProject = project
        openEditor = editor
        scope.launch(Dispatchers.EDT) {
            val obstacles = createLevel()
            val maxWidth = obstacles.filter { it.width != editor.contentComponent.width }.maxOf { it.width }
            val (p1, p2) = spawnPlayers(maxWidth)
            val (g1, g2) = createGoals()
            ball = spawnBall(p2, CollisionChecker(listOf(p1, p2), listOf(g1, g2), obstacles))
            input = PongInput(p1, FPS) {
                stop()
            }

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(input)

            pongComponent = attachGameToEditor(editor, obstacles, ball, p1, p2, g1, g2)
                .apply { start() }

            writeScore()
            openDocument = editor.document
        }
    }

    fun updateScore(index: Int) {

        openDocument.getLineStartOffset(textLine)
        if(index == 0) score1++ else score2++
        TextWriter.replaceText(
            startOffset = openDocument.getLineStartOffset(textLine),
            endOffset = openDocument.getLineEndOffset(textLine),
            text = "// **************************************** Player One: $score2 - $score1 :Player Two **************************************** //",
            document = openDocument,
            project = openProject
        )
    }

    fun stop() {
        ball.stop()
        pongComponent.stop()
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(input)
        openEditor.contentComponent.remove(pongComponent)
        openEditor.contentComponent.repaint()
        openEditor.contentComponent.revalidate()
        score1 = 0
        score2 = 0
        textLine = 0
        onEnd()
    }

    private fun attachGameToEditor(
        editor: Editor, obstacles: MutableList<Obstacle>, ball: Ball, player1: Paddle, player2: Paddle, g1: Goal, g2: Goal
    ): PongComponent {
        val contentComponent = editor.contentComponent
        val pongComponent = PongComponent(obstacles, ball, player1, player2, scope, FPS).apply {
            bounds = contentComponent.bounds
            isOpaque = false
        }

        contentComponent.add(pongComponent)
        contentComponent.revalidate()
        contentComponent.repaint()
        pongComponent.requestFocusInWindow()
        return pongComponent
    }

    private fun createLevel() : MutableList<Obstacle>  {
        val document = editor.document
        val obstacles = mutableListOf<Obstacle>()
        val lineHeight = editor.lineHeight
        val scrollOffset = editor.scrollingModel.verticalScrollOffset

        for(line in 0 until document.lineCount) {
            val lineStartOffset = document.getLineStartOffset(line)
            val lineEndOffset = document.getLineEndOffset(line)

            val lineTextStartIndex = document.getText(TextRange(lineStartOffset, lineEndOffset)).indexOfFirst { !it.isWhitespace() }

            if(lineTextStartIndex == -1) {
                continue
            }

            val startLogicalPosition = LogicalPosition(line, lineTextStartIndex)

            val startPos = editor.logicalPositionToXY(startLogicalPosition).toVec2(scrollOffset)
            val endPos = editor.offsetToXY(lineEndOffset).toVec2(scrollOffset)
            val width = endPos.x - startPos.x

            obstacles.add(
                Obstacle(
                    position = startPos,
                    width = width.roundToInt(),
                    height = lineHeight,
                )
            )
        }

        // Top side of the map
        obstacles.add(
            Obstacle(
                position = Vec2(0f, scrollOffset.toFloat()),
                width = editor.contentComponent.width,
                height = 5
            )
        )

        // Bottom side of the map
        obstacles.add(
            Obstacle(
                position = Vec2(0f, (editor.component.height + (scrollOffset - 5f))),
                width = editor.contentComponent.width,
                height = 5
            )
        )

        return obstacles
    }

    private fun spawnBall(aiPaddle: Paddle, collider: CollisionChecker): Ball {
        val caretModel = editor.caretModel
        val scrollOffset = editor.scrollingModel.verticalScrollOffset
        val position = editor.offsetToXY(caretModel.offset).toVec2(scrollOffset)
        return Ball(
            position = position,
            collider = collider,
            aiPaddle = aiPaddle,
            service = this,
            scope = scope,
            fps = FPS,
        )
    }

    private fun spawnPlayers(maxWidth: Int): Pair<Paddle, Paddle> {
        val document = editor.document
        val scrollOffset = editor.scrollingModel.verticalScrollOffset
        val line = document.getLineNumber(editor.caretModel.offset)
        val p1BrickPosition = editor.offsetToXY(document.getLineStartOffset(line) + 1)
        val p1 = Paddle(p1BrickPosition.toVec2(scrollOffset))
        val p2 = Paddle(Vec2(p1.position.x + maxWidth + 100, p1.position.y), speed = 0.3f)
        return p1 to p2
    }

    private fun createGoals(): Pair<Goal, Goal> {

        // Left side of the map
        val g1 = Goal(
            position = Vec2.zero,
            height = editor.contentComponent.height,
            goalIndex = 0,
        )

        // Right side of the map
        val g2 = Goal(
            position = Vec2(editor.contentComponent.width - 50f, 0f),
            height = editor.contentComponent.height,
            goalIndex = 1,
        )
        return g1 to g2
    }

    private fun writeScore(){
        val document = editor.document
        for(line in 0 until document.lineCount) {
            val lineStartOffset = document.getLineStartOffset(line)
            val lineEndOffset = document.getLineEndOffset(line)

            if(document.getText(TextRange(lineStartOffset, lineEndOffset)).isEmpty()){
                textLine = line
                TextWriter.writeText(
                    offset = lineStartOffset,
                    text = "// **************************************** Player One: $score1 - $score2 :Player Two **************************************** //",
                    document = document,
                    project = project
                )
                return
            }
        }
    }
}