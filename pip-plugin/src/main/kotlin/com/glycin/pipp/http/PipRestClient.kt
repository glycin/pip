package com.glycin.pipp.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private const val baseUrl = "http://localhost:1337/"

object PipRestClient {
    private val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
    }

    suspend fun doCodeQuestion(codeRequest: CodingRequestBody): String? {
        val response = client.post("$baseUrl/code/generate") {
            contentType(ContentType.Application.Json)
            setBody(codeRequest)
        }

        return if(response.status == HttpStatusCode.OK) response.bodyAsText()
        else null
    }

    suspend fun doCodeQuestionStream(codeRequest: CodingRequestBody): Flow<String> {
        return client.post("$baseUrl/code/generate") {
            contentType(ContentType.Application.Json)
            setBody(codeRequest)
        }.streamAsFlow()
    }

    fun close() {
        client.close()
    }

    private fun HttpResponse.streamAsFlow(): Flow<String> = flow {
        if (status != HttpStatusCode.OK) return@flow
        val channel = bodyAsChannel()
        val dataPrefix = "data:"
        while(!channel.isClosedForRead) {
            val line = channel.readUTF8Line()
            if(line != null && line.startsWith(dataPrefix)) {
                emit(line.removePrefix(dataPrefix).trim())
            }
        }
    }
}