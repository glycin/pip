package com.glycin.pipserver

import com.glycin.pipserver.shared.PipRequestBody
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

    @PostMapping("/help")
    fun generate(
        @RequestBody requestBody: PipRequestBody,
    ): ResponseEntity<String> {
        val response = pipService.requestHelp(requestBody)
        return if(response.isNullOrEmpty())
            ResponseEntity.noContent().build()
        else
            ResponseEntity.ok().body(response)
    }
}