#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
VECTOR_DB_DIR="$SCRIPT_DIR/pip-scripts/qdrant"
EMBED_DIR="$SCRIPT_DIR/pip-scripts/python/embeddingModules"
GRAPH_DIR="$SCRIPT_DIR/pip-graph"

# Start vectordb
(cd "$VECTOR_DB_DIR" && docker compose up -d) &

# Start graph vite server
osascript -e "tell app \"Terminal\" to do script \"cd '$GRAPH_DIR' && npx vite\"" &

# Start Embeddings API (activate venv, then run fastapi)
osascript -e "tell app \"Terminal\" to do script \"cd '$EMBED_DIR' && source ./bin/activate && fastapi run ./embeddingServer.py\"" &

wait
echo "All servers started."
