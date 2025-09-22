package com.glycin.pipserver.retro

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("doom")
class RetroController(
    private val retroService: RetroService,
) {

    @PostMapping("/play")
    fun categorize(
        @RequestBody requestBody: RetroRequestBody,
    ): ResponseEntity<RetroPlayResponse> {
        val response = retroService.play(requestBody)
        return ResponseEntity.ok().body(response)
    }
}