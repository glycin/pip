package com.glycin.pipp.context

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

class CodeGraphBuilder(
    private val project: Project,
) {

    fun build(): CodeGraph {
        val nodes = mutableListOf<CodeNode>()
        val edges = mutableListOf<CodeEdge>()

        ReadAction.run<RuntimeException> {
            val scope = GlobalSearchScope.projectScope(project)

            val shortNamesCache = PsiShortNamesCache.getInstance(project)
            val allClassNames = shortNamesCache.allClassNames
            val seenClasses = HashSet<PsiClass>()

            for(name in allClassNames) {
                shortNamesCache.getClassesByName(name, scope).forEach { psiClass ->
                    if(!seenClasses.add(psiClass)) return@forEach
                    if(!psiClass.isValid || psiClass.qualifiedName == null) return@forEach

                    val classNodeId = psiClass.toClassId()
                    nodes += CodeNode(
                        id = classNodeId,
                        type = CodeNodeType.CLASS,
                        name = psiClass.name ?: "<anonymous>",
                        fqName = psiClass.qualifiedName ?: psiClass.name ?: "<anonymous>",
                        filePath = psiClass.containingFile?.virtualFile?.path,
                        lineNumber = offsetToLine(psiClass, psiClass.textOffset),
                    )

                    psiClass.methods.forEach { method ->
                        if(method.isValid) {
                            val methodNodeId = method.toMethodId()
                            nodes += CodeNode(
                                id = methodNodeId,
                                type = CodeNodeType.METHOD,
                                name = method.name,
                                fqName = method.fqMethodName(),
                                parentId = classNodeId,
                                filePath = method.containingFile?.virtualFile?.path,
                                lineNumber = offsetToLine(method, method.textOffset),
                            )

                            edges += CodeEdge(
                                source = classNodeId,
                                target = methodNodeId,
                                type = CodeEdgeType.CONTAINS
                            )

                            addInvocationEdges(method, edges)
                        }
                    }
                }
            }
        }

        return CodeGraph(nodes, dedupeEdges(edges))
    }

    private fun PsiClass.toClassId(): String = "class:${qualifiedName ?: System.identityHashCode(this)}"

    private fun PsiMethod.toMethodId(): String = "method:${this.fqMethodName()}"

    private fun PsiMethod.fqMethodName(): String {
        val cls = containingClass?.qualifiedName ?: "<anon>"
        val sig = buildString {
            append(name)
            append("(")
            append(parameterList.parameters.joinToString(",") { it.type.presentableText })
            append(")")
        }
        return "$cls#$sig"
    }

    private fun offsetToLine(element: PsiElement, offset: Int): Int? {
        val file = element.containingFile?.virtualFile ?: return null
        val doc = FileDocumentManager.getInstance().getDocument(file) ?: return null
        return doc.getLineNumber(offset) + 1
    }

    private fun addInvocationEdges(method: PsiMethod, edges: MutableList<CodeEdge>) {
        val callerId = method.toMethodId()
        method.body?.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                val resolved = expression.resolveMethod()
                if (resolved != null && resolved.isValid) {
                    val calleeId = resolved.toMethodId()
                    edges += CodeEdge(
                        source = callerId,
                        target = calleeId,
                        type = CodeEdgeType.INVOKES
                    )
                }
            }
        })
    }

    private fun dedupeEdges(edges: List<CodeEdge>): List<CodeEdge> {
        return edges.distinctBy { Triple(it.source, it.target, it.type) }
    }
}