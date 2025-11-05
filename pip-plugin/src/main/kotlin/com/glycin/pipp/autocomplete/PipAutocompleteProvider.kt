package com.glycin.pipp.autocomplete

import com.glycin.pipp.settings.PipSettings
import com.glycin.pipp.ui.PipInputDialog
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ProcessingContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.swing.Icon

private val PIP_ICON: Icon = IconLoader.getIcon("/art/icons/pip.png", PipInputDialog::class.java)

class PipCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    val settings = ApplicationManager.getApplication().getService(PipSettings::class.java)
                    if(!settings.state.enableAutoCritique) { return }
                    val project = parameters.position.project
                    val inlineService = project.getService(InlineService::class.java) ?: return

                    val editor = parameters.editor
                    val document = editor.document
                    val caretOffset = parameters.offset
                    val lineNumber = document.getLineNumber(caretOffset)
                    val lineStartOffset = document.getLineStartOffset(lineNumber)
                    val lineEndOffset = document.getLineEndOffset(lineNumber)
                    val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                    runBlocking {
                        withTimeoutOrNull(5000) {
                            try {
                                val suggestion = inlineService.autocomplete(lineText)
                                suggestion?.let {
                                    val lookupElement = LookupElementBuilder.create(it)
                                        .withIcon(PIP_ICON)
                                        .withTypeText("Pip Autocritique")
                                        .withBoldness(true)
                                    val prioritized = PrioritizedLookupElement.withPriority(lookupElement, 100000.0)
                                    resultSet.addElement(prioritized)
                                }
                            } catch (e: Exception) { }
                        }
                    }
                }
            }
        )
    }
}