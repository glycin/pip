package com.glycin.pipserver.qdrant

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class EmbeddingService {

    private val objectMapper = ObjectMapper().registerKotlinModule().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    }

    private val client = RestClient.builder()
        .baseUrl("http://localhost:8000")
        .requestFactory(SimpleClientHttpRequestFactory())
        .messageConverters { converters ->
            converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
        .build()

    fun getEmbeddings(request: EmbeddingRequest): EmbeddingResponse? {
        return try {
            client.post()
                .uri("/multi-embed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse::class.java)
        } catch (e: Exception) {
            println("Error calling the pip vectorizer: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}