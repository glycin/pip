package com.glycin.pipscripts.vectorization

import com.glycin.pipscripts.slack.SlackTrimmedMessage
import java.util.UUID

class Vectorizer(
    private val messages: List<SlackTrimmedMessage>,
    private val batchSize: Int = 10,
    private val embeddingService: EmbeddingWebService,
    private val qdrantConnector: QdrantConnector,
) {

    fun parseText(): String {
        var count = 0
        qdrantConnector.initCollection()
        val chunks = messages.chunked(batchSize)
        val skipped = mutableListOf<String>()
        chunks.forEach { chunk ->
            val texts = chunk.associate { it.id to it.message }
            val uniqueTexts = texts.filterNot { qdrantConnector.checkIfItemExists(UUID.fromString(it.key)) }.map { it.value }
            if(uniqueTexts.isNotEmpty()) {
                val request = EmbeddingRequest(uniqueTexts, dense = true, sparse = true, colbert = true)
                embeddingService.getEmbeddings(request)?.let { response ->
                    val uniqueSlackMessages = chunk.filter { uniqueTexts.contains(it.message) }
                    val vectorsDocs = uniqueSlackMessages.associateWith { c ->
                        response.results.first { it.text == c.message}
                    }.mapNotNull { (key, value) ->
                        if(value.dense.size > 1024) {
                            println("Doc ${key.id} has a dense vector size of ${value.dense.size}")
                            skipped.add(key.id)
                            return@mapNotNull null
                        }
                        if(value.colbert.size > 1024) {
                            println("Doc ${key.id} has a colbert vector size of ${value.colbert.size}")
                            skipped.add(key.id)
                            return@mapNotNull null
                        }

                        if(value.sparse.size > 1024) {
                            println("Doc ${key.id} has a sparse vector size of ${value.sparse.size}")
                            skipped.add(key.id)
                            return@mapNotNull null
                        }
                        value.toVectorDocument(key)
                    }
                    if(vectorsDocs.isNotEmpty()) {
                        qdrantConnector.storeBatch(vectorsDocs)
                    }
                }
            } else {
                println("No unique texts in batch!")
            }
            count++
            println("Finished chunk $count/${chunks.size}")
        }

        println("Done! ${skipped.size} vectors were skipped: ${skipped.joinToString()}")
        return "Put all vectors in qdrant!"
    }

    private fun EmbeddingResult.toVectorDocument(slackMessage: SlackTrimmedMessage): VectorDocument {
        return VectorDocument(
            id = UUID.fromString(slackMessage.id),
            timestamp = slackMessage.timestamp,
            userName = slackMessage.userName,
            text = text,
            denseVector = dense.map { it.toFloat() },
            sparseVector = sparse.mapKeys { (key, _) ->
                key.toInt()
            }.mapValues { (_, value) ->
                value.toFloat()
            },
            colbertVectors = colbert.map {
                it.map { v -> v.toFloat() }
            }
        )
    }
}