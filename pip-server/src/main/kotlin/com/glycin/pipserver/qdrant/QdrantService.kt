package com.glycin.pipserver.qdrant

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Points
import io.qdrant.client.grpc.Points.Condition
import io.qdrant.client.grpc.Points.QueryPoints
import io.qdrant.client.grpc.Points.DenseVector
import io.qdrant.client.grpc.Points.FieldCondition
import io.qdrant.client.grpc.Points.MultiDenseVector
import io.qdrant.client.grpc.Points.PrefetchQuery
import io.qdrant.client.grpc.Points.Query
import io.qdrant.client.grpc.Points.ScoredPoint
import io.qdrant.client.grpc.Points.SparseVector
import io.qdrant.client.grpc.Points.VectorInput
import io.qdrant.client.grpc.Points.WithPayloadSelector
import io.qdrant.client.grpc.Points.WithVectorsSelector
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val SLACK_COLLECTION = "pip_slack"
private const val PREFILTER_LIMIT = 50L
private const val RESULT_LIMIT = 3L

@Service
class QdrantService(
    private val embeddingService: EmbeddingService,
) {
    private val qdrant = QdrantClient(
        QdrantGrpcClient.newBuilder("localhost", 6334, false).build()
    )

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun search(query: String, usernameFilter: String?): List<QdrantSearchDto> {
        val vectors = embed(query) ?: return emptyList()
        val dense = vectors.results.first().dense
        val sparse = vectors.results.first().sparse
        val colbert = vectors.results.first().colbert

        val densePrefetch = PrefetchQuery.newBuilder()
            .setQuery(
                Query.newBuilder()
                    .setNearest(
                        VectorInput.newBuilder()
                            .setDense(
                                DenseVector.newBuilder().addAllData(dense).build()
                            )).build())
            .setUsing("dense")
            .setLimit(PREFILTER_LIMIT)
            .build()

        val sparsePrefetch = PrefetchQuery.newBuilder()
            .setQuery(
                Query.newBuilder()
                    .setNearest(
                        VectorInput.newBuilder()
                            .setSparse(
                                SparseVector.newBuilder()
                                    .addAllIndices(sparse.keys)
                                    .addAllValues(sparse.values)
                                    .build())))
            .setUsing("sparse")
            .setLimit(PREFILTER_LIMIT)
            .build()

        val multivectorFetch = VectorInput.newBuilder()
            .setMultiDense(
                MultiDenseVector.newBuilder()
                    .addAllVectors(
                        colbert.map {
                            DenseVector.newBuilder().addAllData(it).build()
                        }
                    )
                    .build()
            ).build()

        val queryPoints = QueryPoints.newBuilder()
            .setCollectionName(SLACK_COLLECTION)
            .addPrefetch(densePrefetch)
            .addPrefetch(sparsePrefetch)
            .setQuery(Query.newBuilder().setNearest(multivectorFetch).build())
            .setUsing("colbert")
            .setLimit(RESULT_LIMIT)
            .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
            .setWithVectors(WithVectorsSelector.newBuilder().setEnable(false).build())

        usernameFilter?.let {
            val filter = Points.Filter.newBuilder()
                .addMust(
                    Condition.newBuilder().setField(
                        FieldCondition.newBuilder()
                            .setKey("username")
                            .setMatch(
                                Points.Match.newBuilder()
                                    .setText(it)
                                    .build()
                            )
                            .build()
                    ).build()
                )
                .build()
            queryPoints.setFilter(filter)
        }

        return qdrant.queryAsync(queryPoints.build()).get()
            .mapNotNull { it.toDto() }
    }

    private fun embed(query: String): EmbeddingResponse? {
        return embeddingService.getEmbeddings(
            EmbeddingRequest(
                texts = listOf(query),
                dense = true,
                sparse = true,
                colbert = true
            )
        )
    }

    private fun ScoredPoint.toDto(): QdrantSearchDto? {
        val text = payloadMap["text"]?.stringValue
        val timestamp = payloadMap["timestamp"]?.stringValue?.let { LocalDateTime.parse(it, formatter) }
        val username = payloadMap["username"]?.stringValue

        if(text == null || timestamp == null || username == null){
            return null
        }

        return QdrantSearchDto(
            text = text,
            timestamp = timestamp,
            userName = username,
            score = score
        )
    }
}