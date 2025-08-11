from FlagEmbedding import BGEM3FlagModel
from pydantic import BaseModel
from typing import List
from fastapi import FastAPI
import numpy as np

class EmbeddingRequest(BaseModel):
    texts: List[str]
    dense: bool = False
    sparse: bool = False
    colbert: bool = True

model = BGEM3FlagModel('BAAI/bge-m3', use_fp16=False) #Set to true for GPU inference

app = FastAPI()
"""
@app.middleware("http")
async def log_requests(request: Request, call_next):
    # Log the raw request details
    body = await request.body()
    print(f"Raw request body: {body}")
    print(f"Content-Type: {request.headers.get('content-type')}")
    print(f"Headers: {dict(request.headers)}")
    
    # Important: Create a new request with the body for FastAPI to process
    async def receive():
        return {"type": "http.request", "body": body}
    
    request._receive = receive
    response = await call_next(request)
    return response
"""

@app.post("/multi-embed")
async def embed_text(req: EmbeddingRequest):
    print(f"Embedding {len(req.texts)} texts")
    if( not req.texts):
        return {"results": []}
    
    out = model.encode(
        req.texts,
        batch_size=12,
        max_length=8192, # reduce if shorter texts for speed
        return_dense=req.dense, 
        return_sparse=req.sparse, 
        return_colbert_vecs=req.colbert
    )

    dense_items = out.get('dense_vecs', []) if req.dense else []
    sparse_items = out.get('lexical_weights', []) if req.sparse else [] #Sparse vectors are returned as lexical weights
    colbert_items = out.get('colbert_vecs', []) if req.colbert else []
    
    print(f"Processing {len(req.texts)} texts")
    results = []
    for i, text in enumerate(req.texts):
        result = {"text": text}
        
        # Handle dense vectors
        if req.dense and len(dense_items) > 0:
            dense_vec = dense_items[i]
            result["dense"] = dense_vec.tolist() if isinstance(dense_vec, np.ndarray) else list(dense_vec)
        else:
            result["dense"] = []
        
        # Handle sparse vectors
        if req.sparse and len(sparse_items) > 0:
            sparse_vec = sparse_items[i]
            if isinstance(sparse_vec, dict):
                # Convert numpy.float32 values to regular Python floats
                result["sparse"] = {str(k): float(v) for k, v in sparse_vec.items()}
            else:
                result["sparse"] = sparse_vec.tolist() if isinstance(sparse_vec, np.ndarray) else list(sparse_vec)
        else:
            result["sparse"] = []
        
        # Handle ColBERT vectors
        if req.colbert and len(colbert_items) > 0:
            colbert_item = colbert_items[i]
            if isinstance(colbert_item, dict) and "vecs" in colbert_item:
                vecs = colbert_item["vecs"]
                vecs_list = vecs.tolist() if isinstance(vecs, np.ndarray) else list(vecs)
                result["colbert"] = vecs_list
            else:
                # item is likely a NumPy ndarray [num_tokens, dim]
                vecs_list = colbert_item.tolist() if isinstance(colbert_item, np.ndarray) else list(colbert_item)
                result["colbert"] = vecs_list
        else:
            result["colbert"] = []
        
        results.append(result)

    return {"results": results}