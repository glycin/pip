@echo off
setlocal

set VECTOR_DB_DIR=C:\Projects\pip\pip-scripts\qdrant
set EMBED_DIR=C:\Projects\pip\pip-scripts\python\embeddingModules
set GRAPH_DIR=C:\Projects\pip\pip-graph

rem Start vectordb in a new window
start "Docker Compose" cmd /k "cd /d "%VECTOR_DB_DIR%" && docker compose up -d"

rem Start graph vite server in a new window
start "Vite Dev Server" cmd /k "cd /d "%GRAPH_DIR%" && npx vite"

rem Start Embeddings API (activate venv, then run fastapi dev embeddingServer.py)
rem Note: using 'call' so the activate.bat doesn't swallow the rest of the chain
start "Embeddings API" cmd /k "cd /d "%EMBED_DIR%" && call .\Scripts\activate && fastapi run .\embeddingServer.py"

endlocal
