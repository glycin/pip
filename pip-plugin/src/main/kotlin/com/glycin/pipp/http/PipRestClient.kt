package com.glycin.pipp.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Base64

private const val baseUrl = "http://localhost:1337"

object PipRestClient {
    private val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }

        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }

        engine {
            requestTimeout = 60_000
        }
    }

    suspend fun getCategory(pipRequestBody: PipRequestBody): CategorizationDto? {
        val response = client.post("$baseUrl/pip/categorize") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(pipRequestBody)
        }

        return if(response.status == HttpStatusCode.OK) response.body()
        else null
    }

    suspend fun doQuestion(pipRequestBody: PipRequestBody): PipResponse? {
        val response = client.post("$baseUrl/pip/help") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(pipRequestBody)
        }

        return if(response.status == HttpStatusCode.OK) response.body()
        else null
    }

    suspend fun doPrank(prankRequestBody: PipPrankRequestBody): PipPrankResponseDto? {
        val response = client.post("$baseUrl/pip/prank") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(prankRequestBody)
        }

        return if(response.status == HttpStatusCode.OK) response.body()
        else null
    }

    suspend fun doPasteReview(pipPasteBody: PipPasteBody): PipResponse? {
        val response = client.post("$baseUrl/pip/paste") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(pipPasteBody)
        }

        return if(response.status == HttpStatusCode.OK) response.body()
        else null
    }

    suspend fun doCodeQuestion(codeRequest: PipRequestBody): PipResponse? {
        val response = client.post("$baseUrl/code/generate") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(codeRequest)
        }

        return if(response.status == HttpStatusCode.OK) response.body()
        else null
    }

    fun doCodeQuestionStream(pipRequest: PipRequestBody): Flow<String> = callbackFlow {
        client.sse(urlString = "$baseUrl/code/generate/stream", request = {
            method = HttpMethod.Post
            setBody(pipRequest)
            contentType(ContentType.Application.Json)
        }) {
            incoming.collect { event ->
                event.data?.let {
                    trySend(String(Base64.getDecoder().decode(it)))
                }
            }
        }

        awaitClose {  }
    }

    fun close() {
        client.close()
    }
}