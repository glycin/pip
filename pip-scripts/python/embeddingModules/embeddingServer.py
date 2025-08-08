from FlagEmbedding import BGEM3FlagModel
from pydantic import BaseModel
from typing import List
from fastapi import FastAPI
import numpy as np

class EmbeddingRequest(BaseModel):
    texts: List[str]

model = BGEM3FlagModel('BAAI/bge-m3', use_fp16=False) #Set to true for GPU inference

app = FastAPI()

@app.get("/multi-embed")
async def embed_text(req: EmbeddingRequest):
    print(f"Embedding {len(req.texts)} texts")
    if( not req.texts):
        return {"embedding": []}
    
    out = model.encode(
        req.texts,
        batch_size=12,
        max_length=8192, # reduce if shorter texts for speed
        return_dense=False, 
        return_sparse=False, 
        return_colbert_vecs=True
    )

    colbert_items = out['colbert_vecs']
    print(len(colbert_items))
    for i in range(len(colbert_items)):
        print(colbert_items[i].shape)

    results = []
    for text, item in zip(req.texts, colbert_items):
        if isinstance(item, dict) and "vecs" in item:
            vecs = item["vecs"]
            vecs_list = vecs.tolist() if isinstance(vecs, np.ndarray) else list(vecs)
            results.append({
                "text": text,
                "vecs": vecs_list
            })
        else:
            # item is likely a NumPy ndarray [num_tokens, dim]
            vecs_list = item.tolist() if isinstance(item, np.ndarray) else list(item)
            results.append({
                "text": text,
                "vecs": vecs_list
            })

    return {"results": results}