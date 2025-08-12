package com.glycin.pipscripts.vectorization

import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.VectorFactory.multiVector
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.CreateCollection
import io.qdrant.client.grpc.Collections.HnswConfigDiff
import io.qdrant.client.grpc.Collections.SparseIndexConfig
import io.qdrant.client.grpc.Collections.SparseVectorConfig
import io.qdrant.client.grpc.Collections.SparseVectorParams
import io.qdrant.client.grpc.Collections.VectorParams
import io.qdrant.client.grpc.Collections.VectorsConfig
import io.qdrant.client.VectorFactory.vector
import io.qdrant.client.VectorsFactory.namedVectors
import io.qdrant.client.grpc.JsonWithInt
import io.qdrant.client.grpc.Points
import io.qdrant.client.grpc.Points.PointStruct
import io.qdrant.client.grpc.Points.ReadConsistency
import org.springframework.stereotype.Service
import java.util.*

@Service
class QdrantConnector {
    private val qdrant = QdrantClient(
        QdrantGrpcClient.newBuilder("localhost", 6334, false).build()
    )

    fun storeBatch(docs: List<VectorDocument>) {
        val payloads = docs.map { vd ->
            PointStruct.newBuilder()
                .setId(id(vd.id))
                .setVectors(
                    namedVectors(
                        mapOf(
                            "dense" to vector(vd.denseVector),
                            "sparse" to vector(vd.sparseVector.values.toList(), vd.sparseVector.keys.toList()),
                            "colbert" to multiVector(vd.colbertVectors)
                        )
                    )
                )
                .putAllPayload(
                    mapOf(
                        "timestamp" to JsonWithInt.Value.newBuilder().setStringValue(vd.timestamp).build(),
                        "username" to JsonWithInt.Value.newBuilder().setStringValue(vd.userName).build(),
                        "text" to JsonWithInt.Value.newBuilder().setStringValue(vd.text).build()
                    )
                )
                .build()
        }


        val result = qdrant.upsertAsync(SLACK_COLLECTION, payloads).get()
        if(result.status != Points.UpdateStatus.Completed) {
            println("Something went wrong when trying to add vectors with id's -> ${docs.joinToString { it.id.toString() }}")
        }
    }

    fun initCollection() {
        val collectionExists = qdrant.collectionExistsAsync(SLACK_COLLECTION).get()
        if(!collectionExists) {
            val collectionCreateRequest = CreateCollection.newBuilder()
                .setCollectionName(SLACK_COLLECTION)
                .setVectorsConfig(
                    VectorsConfig.newBuilder()
                        .setParamsMap(
                            Collections.VectorParamsMap.newBuilder()
                                .putMap("dense", VectorParams.newBuilder()
                                    .setSize(1024)
                                    .setDistance(Collections.Distance.Cosine)
                                    .build()
                                )
                                .putMap("colbert", VectorParams.newBuilder()
                                    .setSize(1024)
                                    .setDistance(Collections.Distance.Cosine)
                                    .setMultivectorConfig(Collections.MultiVectorConfig.newBuilder()
                                        .setComparator(Collections.MultiVectorComparator.MaxSim)
                                        .build()
                                    )
                                    .setHnswConfig(HnswConfigDiff.newBuilder()
                                        .setM(0) //Disable HNSW indexing
                                        .build()
                                    )
                                    .build()
                                )
                        )
                )
                .setSparseVectorsConfig(
                    SparseVectorConfig.newBuilder()
                        .putMap("sparse", SparseVectorParams.newBuilder()
                            .setIndex(SparseIndexConfig.newBuilder()
                                .setOnDisk(false)
                                .build())
                            .build())
                        .build()
                )
                .build()
            val collection = qdrant.createCollectionAsync(collectionCreateRequest).get()
            if(collection.result) {
                println("Created $SLACK_COLLECTION qdrant collection!")
            }
        } else {
            println("$SLACK_COLLECTION already exists, skipping creation!")
        }
    }

    fun checkIfItemExists(id: UUID): Boolean {
        return qdrant.retrieveAsync(
            SLACK_COLLECTION,
            id(id),
            false,
            false,
            ReadConsistency.newBuilder().setFactor(1).build()
        ).get().isNotEmpty()
    }

    companion object {
        const val SLACK_COLLECTION = "pip_slack"
    }
}