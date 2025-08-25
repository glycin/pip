package com.glycin.pipp.context

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod

data class JavaSelection(
    val methods: List<PsiMethod>,
    val classes: List<PsiClass>,
    val selectedText: String? = null,
) {
    companion object {
        val EMPTY = JavaSelection(emptyList(), emptyList())
    }
}