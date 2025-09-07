package com.glycin.pipp.paste

import com.glycin.pipp.Manager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

class PasteHandler(
    val originalHandler: EditorActionHandler,
    private val manager: Manager,
): EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null)

        if(contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            val text = contents.getTransferData(DataFlavor.stringFlavor) as String
            val offset = editor.caretModel.offset
            val line = editor.offsetToVisualLine(offset, true)
            manager.interceptPaste(text, line, offset)
        } else {
            originalHandler.execute(editor, caret, dataContext)
        }
    }
}