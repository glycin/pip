package com.glycin.pipp

import com.glycin.pipp.utils.Extensions.getAndRemoveBetween
import com.glycin.pipp.utils.Extensions.getAndRemoveCodeBlock
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
    private val scope: CoroutineScope,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
) {

    fun processResponse(response: String) {
        val (thinkBody, remaining) = response.getAndRemoveBetween("<think>", "</think>")
        val (codeBody, cleaned) = remaining.getAndRemoveCodeBlock()

        scope.launch(Dispatchers.EDT) {
            TextWriter.deleteText(0, editor.document.textLength, editor, project)
            pip.changeStateTo(PipState.TYPING)
            delay(5000)
            TextWriter.writeText(0, codeBody, editor, project)
            delay(1000)
            pip.changeStateTo(PipState.SITTING)
            agentComponent.showSpeechBubble(cleaned)
        }
    }
}