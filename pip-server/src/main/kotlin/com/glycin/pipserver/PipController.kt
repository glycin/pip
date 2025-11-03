package com.glycin.pipserver

import com.glycin.pipserver.shared.CategorizationDto
import com.glycin.pipserver.shared.PipPasteBody
import com.glycin.pipserver.shared.PipPrankRequestBody
import com.glycin.pipserver.shared.PipPrankResponseDto
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.PipResponse
import com.glycin.pipserver.shared.PipTicTacToeResponseDto
import com.glycin.pipserver.shared.TicTacToeRequestBody
import com.glycin.pipserver.util.Emojis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val LOG = KotlinLogging.logger {}

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
        LOG.info { "Done! ${Emojis.checkmark}" }
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/prank")
    fun prank(
        @RequestBody requestBody: PipPrankRequestBody,
    ): ResponseEntity<PipPrankResponseDto> {
        val response = pipService.requestPrank(requestBody)
        LOG.info { "Done! ${Emojis.checkmark}" }
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/paste")
    fun paste(
        @RequestBody requestBody: PipPasteBody,
    ): ResponseEntity<PipResponse> {
        val response = pipService.requestPasteReview(requestBody)
        LOG.info { "Done! ${Emojis.checkmark}" }
        return ResponseEntity.ok().body(response)
    }

    @PostMapping("/tictactoe")
    fun ticTacToe(
        @RequestBody requestBody: TicTacToeRequestBody,
    ): ResponseEntity<PipTicTacToeResponseDto> {
        val response = pipService.playTicTacToe(requestBody)
        return ResponseEntity.ok().body(response)
    }
}