package com.glycin.pipserver.coder

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("code")
class CoderController(
    private val coderService: CoderService,
){
    @GetMapping("/generate")
    fun generate(
        @RequestParam(value = "input") input: String,
        @RequestParam(value = "think", defaultValue = "false") think: Boolean,
        @RequestParam(value = "chatId") chatId: String?,
    ): String? {
        return coderService.generate(input, think, chatId)
    }

    @GetMapping("/generate/stream")
    fun generateStream(
        @RequestParam(value = "input") input: String,
        @RequestParam(value = "think", defaultValue = "false") think: Boolean,
        @RequestParam(value = "chatId") chatId: String?,
    ): Flux<String>? {
        return coderService.generateStream(input, think, chatId)
    }
}