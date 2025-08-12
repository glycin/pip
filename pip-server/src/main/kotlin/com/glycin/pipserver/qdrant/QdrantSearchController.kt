package com.glycin.pipserver.qdrant

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("search")
class QdrantSearchController(
    private val qdrantService: QdrantService,
) {

    @GetMapping
    fun searchQdrant(
        @RequestParam query: String,
        @RequestParam(required = false) usernameFilter: String? = null,
    ): ResponseEntity<List<QdrantSearchDto>> {
        val result = qdrantService.search(query, usernameFilter)
        return ResponseEntity.ok(result)
    }
}