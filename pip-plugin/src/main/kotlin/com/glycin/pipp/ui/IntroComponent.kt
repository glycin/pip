package com.glycin.pipp.ui

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.JLabel

class IntroComponent(
    private val scope: CoroutineScope,
    private val onClose: (IntroComponent) -> Unit,
): JComponent(), Disposable {

    private var matrixGif: JLabel? = null

    fun showIntro() {
        isOpaque = false
        scope.launch(Dispatchers.Default) {
            delay(500)
            addMatrix()
            delay(5000)
            hideMatrix()
        }
    }

    private fun addMatrix() {
        matrixGif = JLabel(Gifs.MATRIX_GIF).apply {
            setBounds(0, 0, 1920, 1080)
        }
        add(matrixGif)
        revalidate()
        repaint()
    }

    private fun hideMatrix() {
        if(matrixGif == null) return
        remove(matrixGif)
        matrixGif = null
        revalidate()
        repaint()
        dispose()

        onClose.invoke(this)
    }

    override fun dispose() {
        matrixGif = null
    }
}