package com.glycin.pipp.utils

import com.glycin.pipp.http.CategorizationDto
import com.glycin.pipp.http.PipRequestBody
import com.intellij.ui.JBColor
import java.awt.Color

object Extensions {

    fun Color.toJbColor() = JBColor(this, this)
    
    fun PipRequestBody.addCategory(categorizationDto: CategorizationDto) = PipRequestBody(
        input = input,
        think = think,
        chatId = chatId,
        category = categorizationDto.category,
        categoryReason = categorizationDto.reason,
    )
}