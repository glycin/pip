package com.glycin.pipp.ui

import javax.swing.ImageIcon

object Gifs {

    val CAT_JAM_GIF : ImageIcon by lazy {
        val url = checkNotNull(Gifs::class.java.getResource("/art/gifs/cat-jam.gif"))
        ImageIcon(url)
    }

    val MATRIX_GIF : ImageIcon by lazy {
        val url = checkNotNull(Gifs::class.java.getResource("/art/gifs/matrix-effect.gif"))
        ImageIcon(url)
    }
}