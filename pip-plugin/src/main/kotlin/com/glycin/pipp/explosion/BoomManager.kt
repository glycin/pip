package com.glycin.pipp.explosion

import com.glycin.pipp.toVec2
import com.glycin.pipp.utils.SpriteSheetImageLoader
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.util.TextRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import kotlin.math.abs

private const val FPS = 120L
private const val EXP_1_SIZE = 18
private const val EXP_STRENGTH = 30

class BoomManager(
    private val scope: CoroutineScope,
): Disposable {

    private val explosion1 = arrayOfNulls<BufferedImage>(EXP_1_SIZE)

    init {
        loadExplosionSprites()
    }

    fun explode(mousePosition: Point, editor: Editor) {
        if(editor.document.textLength <= 0) return
        if(editor.document.text.all { it.isWhitespace() || it == '\n' }) return
        val project = editor.project ?: return

        val contentComponent = editor.contentComponent
        editor.settings.isVirtualSpace = true

        val yScroll = editor.scrollingModel.verticalScrollOffset
        val objs = getLinesInRange(editor, mousePosition)
        BoomWriter.clear(editor, project)

        val boomComponent = BoomDrawComponent(
            explosionImages = explosion1,
            explosionObjects = objs,
            position = mousePosition.toVec2(editor.scrollingModel),
            scope = scope,
            fps = FPS,
            finishedCallback = {
                it.cleanup()
                contentComponent.remove(it)
                contentComponent.revalidate()
                contentComponent.repaint()
                contentComponent.requestFocusInWindow()
                scope.launch(Dispatchers.EDT) {
                    BoomWriter.writeText(objs, editor, project, yScroll)
                }
            }
        ).apply {
            bounds = (SwingUtilities.getAncestorOfClass(JScrollPane::class.java, editor.contentComponent) as JScrollPane).viewport.viewRect
            preferredSize = contentComponent.size
            isOpaque = false
        }

        contentComponent.add(boomComponent)
        contentComponent.revalidate()
        contentComponent.repaint()

        boomComponent.requestFocusInWindow()
        editor.settings.isVirtualSpace = false
    }

    override fun dispose() {
        explosion1.drop(EXP_1_SIZE)
    }

    private fun loadExplosionSprites() {
        SpriteSheetImageLoader.loadSprites("/art/spritesheets/boom_1.png", 64, 64, EXP_1_SIZE)
            .forEachIndexed { i, img -> explosion1[i] = img }
    }

    private fun getLinesInRange(editor: Editor, explosionCenter: Point): List<MovableObject> {
        val document = editor.document
        val explosionLine = editor.xyToLogicalPosition(explosionCenter).line

        return (0 until document.lineCount).flatMap { line ->
            val startOffset = document.getLineStartOffset(line)
            val endOffset = document.getLineEndOffset(line)
            val distance = abs(editor.offsetToLogicalPosition(startOffset).line - explosionLine)

            document.getText(TextRange(startOffset, endOffset)).mapIndexedNotNull { index, c ->
                if(c.isWhitespace()) return@mapIndexedNotNull null
                val charPos = editor.offsetToXY(startOffset + index)
                MovableObject(
                    position = charPos.toVec2(editor.scrollingModel),
                    width = getCharWidth(editor, c),
                    height = editor.lineHeight,
                    char = c.toString(),
                    inRange = distance <= EXP_STRENGTH,
                )
            }
        }
    }

    private fun getCharWidth(editor: Editor, c: Char): Int {
        val fontMetrics = editor.contentComponent.getFontMetrics(editor.colorsScheme.getFont(EditorFontType.PLAIN))
        return fontMetrics.charWidth(c)
    }
}