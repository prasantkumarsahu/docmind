# DocMind

Chat with your documents. DocMind is a retrieval-augmented generation (RAG) application: upload PDFs, Word files,
Markdown or text, and ask questions in natural language. Answers stream token by token and cite the exact passages
they were drawn from. Everything runs locally: no document leaves your machine.

## Features

- Upload PDF, DOCX, TXT, MD and HTML files (up to 25 MB)
- Asynchronous ingestion: parsing, token-based chunking, embedding and indexing in pgvector
- Semantic search with an HNSW index and cosine distance, with a similarity threshold to drop weak matches
- Answers grounded only in retrieved context, with numbered citations, page numbers and source snippets
- Says so when the documents do not contain the answer instead of guessing
- Streaming responses over Server-Sent Events
- Conversation memory per chat, so follow-up questions work
- Restrict a question to selected documents
- Validation and consistent RFC 7807 problem responses

## Architecture

```
React (Vite, TypeScript, Tailwind)
        │  REST + SSE
        ▼
Spring Boot 4 + Spring AI 2
  ├─ DocumentController ─► DocumentService ─► IngestionService (@Async)
  │                                             ├─ PDF / Tika readers
  │                                             ├─ TokenTextSplitter
  │                                             └─ VectorStore.add
  └─ ChatController ─► ChatService
                         ├─ Retriever (similarity search + metadata filter)
                         ├─ PromptBuilder (numbered context, citation rules)
                         └─ ChatClient + MessageChatMemoryAdvisor
        │                               │
        ▼                               ▼
PostgreSQL 17 + pgvector          Ollama
(documents table, vector_store)   llama3.2:3b, nomic-embed-text
```

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring AI 2.0, JdbcClient, Flyway |
| Vector store | PostgreSQL 17 with pgvector (768-dimension embeddings, HNSW) |
| Models | Ollama: `llama3.2:3b` for chat, `nomic-embed-text` for embeddings |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, react-markdown |
| Tests | JUnit 5, Mockito, AssertJ |

## Getting started

Requirements: Docker, JDK 21+, Node.js 20+.

```bash
docker compose up -d
docker exec docmind-ollama ollama pull llama3.2:3b
docker exec docmind-ollama ollama pull nomic-embed-text
```

Backend (http://localhost:8080):

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (http://localhost:5173):

```bash
cd frontend
npm install
npm run dev
```

Upload `samples/acme-handbook.md` and ask *"How many days of notice do I need to give?"*.

The first answer can take a while on CPU while the model loads into memory.

## Configuration

Settings live in `backend/src/main/resources/application.yml`.

| Variable / property | Default | Description |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama endpoint |
| `CHAT_MODEL` | `llama3.2:3b` | Chat model |
| `EMBEDDING_MODEL` | `nomic-embed-text` | Embedding model |
| `docmind.retrieval.top-k` | `5` | Passages retrieved per question |
| `docmind.retrieval.similarity-threshold` | `0.5` | Minimum similarity for a passage to be used |
| `docmind.chunking.chunk-size` | `400` | Chunk size in tokens |
| `docmind.memory.max-messages` | `12` | Messages kept per conversation |

Any Ollama model can be swapped in. When changing the embedding model, update `spring.ai.vectorstore.pgvector.dimensions`
and re-ingest the documents.

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/documents` | Upload a file (multipart `file`), returns `202 Accepted` |
| `GET` | `/api/documents` | List documents with status and chunk count |
| `GET` | `/api/documents/{id}` | Get one document |
| `DELETE` | `/api/documents/{id}` | Delete a document and its chunks |
| `POST` | `/api/chat` | Ask a question, returns the answer and sources |
| `POST` | `/api/chat/stream` | Same, streamed as SSE events `sources`, `token`, `done`, `error` |
| `DELETE` | `/api/chat/{conversationId}` | Clear conversation memory |

Example:

```bash
curl -F file=@samples/acme-handbook.md localhost:8080/api/documents

curl -H 'Content-Type: application/json' localhost:8080/api/chat \
  -d '{"conversationId":"demo","question":"What is the remote work policy?"}'
```

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm run lint && npm run build
```

## License

MIT
