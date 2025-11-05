package com.glycin.pipp.autocomplete

import com.glycin.pipp.settings.PipSettings
import com.intellij.codeInsight.inline.completion.InlineCompletionEvent
import com.intellij.codeInsight.inline.completion.InlineCompletionProvider
import com.intellij.codeInsight.inline.completion.InlineCompletionProviderID
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSingleSuggestion
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSuggestion
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.InternalSerializationApi

class PipInlineProvider: InlineCompletionProvider {
    override val id: InlineCompletionProviderID get() = InlineCompletionProviderID("Pip_Auto_Critique")

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        // turn off until we figure out why it isnt working with kotlin
        return false
        val settings = ApplicationManager.getApplication().getService(PipSettings::class.java)
        return settings.state.enableAutoCritique
    }

    @OptIn(InternalSerializationApi::class)
    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        val service = request.editor.project?.getService(InlineService::class.java) ?: return InlineCompletionSuggestion.Empty

        val text: String? = runBlocking {
            withTimeoutOrNull(1500) {
                service.autocomplete(request.currentLineText())
            }
        }

        return if (text.isNullOrBlank()) {
            InlineCompletionSuggestion.Empty
        } else {
            InlineCompletionSingleSuggestion.build {
                emit(InlineCompletionGrayTextElement("  $text"))
            }
        }
    }

    private fun InlineCompletionRequest.currentLineText(): String {
        val doc = editor.document
        val caret = endOffset
        val line = doc.getLineNumber(startOffset)
        val lineStart = doc.getLineStartOffset(line)
        return doc.text.substring(lineStart, caret)
    }
}