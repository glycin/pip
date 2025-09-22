package com.glycin.pipserver.coder

import com.glycin.pipserver.shared.PipRequestBody
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
        @RequestBody codingRequest: PipRequestBody,
    ): ResponseEntity<CoderResponse> {
        val response = coderService.generate(codingRequest)
        return if(response == null)
            ResponseEntity.noContent().build()
        else
            ResponseEntity.ok().body(response)
    }

    @PostMapping("/autocomplete")
    fun generateAutoComplete(
        @RequestBody request: AutocompleteRequest
    ): ResponseEntity<AutocompleteResponse> {
        val response = coderService.autocomplete(request)
        return if(response == null) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(response)
        }
    }

    @PostMapping("/generate/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun generateStream(
        @RequestBody codingRequest: PipRequestBody,
    ): ResponseEntity<Flow<String>> {
        val response = coderService.generateStream(codingRequest)
        return ResponseEntity.ok().body(response)
    }
}