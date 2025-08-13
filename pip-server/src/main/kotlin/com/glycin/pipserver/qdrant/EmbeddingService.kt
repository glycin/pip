package com.glycin.pipserver.qdrant

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class EmbeddingService(
    @Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

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