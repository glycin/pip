package com.glycin.pipp.ui

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.JLabel

class IntroComponent(
    private val scope: CoroutineScope
): JComponent(), Disposable {

    private var active = true
    private var matrixGif: JLabel? = null

    fun showIntro() {
        isOpaque = false
        scope.launch(Dispatchers.Default) {
            delay(5000)
            addMatrix()
            delay(15000)
            hideMatrix()
        }
    }

    private fun addMatrix() {
        println("added matrix")
        matrixGif = JLabel(Gifs.MATRIX_GIF).apply {
            setBounds(0, 0, 1920, 1080)
        }
        add(matrixGif)
        revalidate()
        repaint()
    }

    fun hideMatrix() {
        if(matrixGif == null) return
        remove(matrixGif)
        matrixGif = null
        revalidate()
        repaint()
    }

    override fun dispose() {
        matrixGif = null
        active = false
    }
}