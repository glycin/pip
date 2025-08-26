package com.glycin.pipserver

import com.glycin.pipserver.shared.CategorizationDto
import com.glycin.pipserver.shared.PipPrankRequestBody
import com.glycin.pipserver.shared.PipPrankResponseDto
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.PipResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("pip")
class PipController(
    private val pipService: PipService
) {

    @PostMapping("/categorize")
    fun categorize(
        @RequestBody requestBody: PipRequestBody,
    ): ResponseEntity<CategorizationDto> {
        val response = pipService.categorize(requestBody)
        return if(response == null)
            ResponseEntity.noContent().build()
        else
            ResponseEntity.ok().body(response)
    }

    @PostMapping("/help")
    fun generate(
        @RequestBody requestBody: PipRequestBody,
    ): ResponseEntity<PipResponse> {
        val response = pipService.requestHelp(requestBody)
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/prank")
    fun prank(
        @RequestBody requestBody: PipPrankRequestBody,
    ): ResponseEntity<PipPrankResponseDto> {
        val response = pipService.requestPrank(requestBody)
        return ResponseEntity.ok().body(response)
    }

    //TODO: Add PIP BORED endpoint
}