package com.glycin.pipserver.coder

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("code")
class CoderController(
    private val coderService: CoderService,
){
    @PostMapping("/generate")
    fun generate(
        @RequestBody codingRequest: CodingRequestBody,
    ): String? {
        return coderService.generate(codingRequest)
    }

    @GetMapping("/generate/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun generateStream(
        @RequestBody codingRequest: CodingRequestBody,
    ): Flux<String>? {
        return coderService.generateStream(codingRequest)
    }
}