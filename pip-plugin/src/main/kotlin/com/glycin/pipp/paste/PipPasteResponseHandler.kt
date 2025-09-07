package com.glycin.pipp.paste

import com.glycin.pipp.AgentComponent
import com.glycin.pipp.Pip
import com.glycin.pipp.PipState
import com.glycin.pipp.http.PipPasteBody
import com.glycin.pipp.http.PipResponse
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PipPasteResponseHandler(
    private val request: PipPasteBody,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
    private val caretOffset: Int,
    private val scope: CoroutineScope,
    private val document: Document,
    private val project: Project,
) {

    fun handleResponse(response: PipResponse) {
        val todo = response.code?.first()?.code ?: "//TODO: Wow, for once you tried to paste something useful!"
        val textToPaste = """
            $todo
            ${request.pasteText}
        """.trimIndent()
        scope.launch {
            delay(1000)
            TextWriter.writeText(caretOffset, textToPaste, document, project)
            delay(2000)
            agentComponent.showSpeechBubble(response.response)
            delay(20_000)
            pip.changeStateTo(PipState.YOYO)
            agentComponent.hideSpeechBubble(false)
        }
    }
}