package com.glycin.pipserver.coder

import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("code")
class CoderController(
    private val coderService: CoderService,
){
    @PostMapping("/generate")
    fun generate(
        @RequestBody codingRequest: CodingRequestBody,
    ): ResponseEntity<String> {
        val response = coderService.generate(codingRequest)
        return if(response.isNullOrEmpty())
            ResponseEntity.noContent().build()
        else
            ResponseEntity.ok().body(response)
    }

    @PostMapping("/generate/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun generateStream(
        @RequestBody codingRequest: CodingRequestBody,
    ): ResponseEntity<Flow<String>> {
        val response = coderService.generateStream(codingRequest)
        return ResponseEntity.ok().body(response)
    }
}