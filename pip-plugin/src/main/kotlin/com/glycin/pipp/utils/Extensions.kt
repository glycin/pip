package com.glycin.pipp.utils

import com.glycin.pipp.Vec2
import com.glycin.pipp.context.JavaSelection
import com.glycin.pipp.http.CategorizationDto
import com.glycin.pipp.http.PipRequestBody
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Point

object Extensions {

    fun Color.toJbColor() = JBColor(this, this)
    
    fun PipRequestBody.addCategory(categorizationDto: CategorizationDto) = PipRequestBody(
        input = input,
        think = think,
        chatId = chatId,
        category = categorizationDto.category,
        categoryReason = categorizationDto.reason,
    )

    fun PsiMethod.fqMethodName(): String {
        val cls = containingClass?.qualifiedName ?: "<anon>"
        val sig = buildString {
            append(name)
            append("(")
            append(parameterList.parameters.joinToString(",") { it.type.presentableText })
            append(")")
        }
        return "$cls#$sig"
    }

    fun Project.getSelectedJavaDeclarations(
        fullyContained: Boolean = false // false = intersects; true = fully inside selection
    ): JavaSelection = ReadAction.compute<JavaSelection, RuntimeException> {
        val editor = FileEditorManager.getInstance(this).selectedTextEditor ?: return@compute JavaSelection.EMPTY
        val psiFile = PsiDocumentManager.getInstance(this).getPsiFile(editor.document) ?: return@compute JavaSelection.EMPTY
        val caret = editor.caretModel.currentCaret
        val start = caret.selectionStart
        val end = caret.selectionEnd
        if (end <= start) return@compute JavaSelection.EMPTY

        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText
        if(selectedText.isNullOrEmpty()) return@compute JavaSelection.EMPTY

        val range = TextRange(start, end)
        val startEl = psiFile.findElementAt(range.startOffset) ?: return@compute JavaSelection.EMPTY
        val endEl = psiFile.findElementAt((range.endOffset - 1).coerceAtLeast(range.startOffset)) ?: startEl
        val root = PsiTreeUtil.findCommonParent(startEl, endEl) ?: psiFile

        val methods = LinkedHashSet<PsiMethod>()
        val classes = LinkedHashSet<PsiClass>()

        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                val tr = method.textRange ?: return
                if (if (fullyContained) range.contains(tr) else tr.intersects(range)) {
                    methods += method
                }
                super.visitMethod(method)
            }
            override fun visitClass(aClass: PsiClass) {
                val tr = aClass.textRange ?: return
                if (if (fullyContained) range.contains(tr) else tr.intersects(range)) {
                    classes += aClass
                }
                super.visitClass(aClass)
            }
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                val tr = expression.textRange
                if (tr != null && tr.intersects(range)) {
                    expression.resolveMethod()?.let { methods += it }
                }
                super.visitMethodCallExpression(expression)
            }
        })

        JavaSelection(methods.toList(), classes.toList(), selectedText)
    }

    fun Point.toVec2() = Vec2(x.toFloat(), y.toFloat())
    fun Point.toVec2(scrollOffset: Int) = Vec2(x.toFloat(), y.toFloat() + scrollOffset)
}