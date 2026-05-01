## Doxen — complete project summary

**What it is:** A documentation crawler and knowledge API for AI agents. Ingests library docs from multiple sources, stores them as structured searchable chunks, and exposes a clean API so AI agents can query component documentation on demand.

**Project name:** Doxen
**Java package:** `dev.doxen`

---

### Tech stack

**Backend**
- Java 25 + Spring Boot 4 (Spring Framework 7)
- Spring AI 2.x — embedding model abstraction (OpenAI, Cohere, Ollama)
- Spring Security — API key authentication + rate limiting
- Redis — async crawl job queue + rate limiting
- Jsoup — HTML crawling and parsing
- Flexmark — Markdown parsing
- MapStruct 1.6 — DTO mapping
- Flyway 10 — database migrations
- Springdoc OpenAPI — auto-generated API docs
- Lombok — boilerplate reduction

**Databases**
- PostgreSQL 16 — source of truth (chunks, libraries, crawl jobs, API keys)
- Qdrant — vector + hybrid search index
- Redis — job queue + rate limiting cache

**Frontend**
- React 19 + Vite
- shadcn/ui + Tailwind CSS
- TanStack Query — data fetching and cache
- TanStack Table — chunk and library browser
- Recharts — crawl stats and analytics
- Zustand — UI state management

**Infrastructure (local dev)**
- Docker Compose — runs Postgres, Qdrant, Redis together
- Ollama — local embedding model (no API key needed in dev)

---

### Input sources

| Source | Format | How |
|---|---|---|
| Local files | `.md`, `.mdx` | File upload or path |
| Docs sites | `.html` | Jsoup URL crawler |
| npm packages | README | npm registry API |
| Maven packages | README | Maven Central API |
| JSDoc / Javadoc | HTML output | Jsoup parser |
| Bulk upload | `.zip` of `/docs` | Extracted + parsed |

---

### Ingestion pipeline

```
Raw source
    ↓
Parse          (Jsoup for HTML · Flexmark for Markdown)
    ↓
Chunk          (split by heading hierarchy)
    ↓            each chunk carries:
    ↓            · library · version · component
    ↓            · heading path · token count
Embed          (Spring AI → vector float[])
    ↓
Store
  ├── PostgreSQL   (source of truth)
  └── Qdrant       (search index)
```

Re-crawl runs on schedule via Redis queue. Only changed content is re-indexed.

---

### Search

Hybrid search — vector + full-text fused with Reciprocal Rank Fusion (RRF):

- **Vector search** — Qdrant semantic similarity on embeddings
- **Full-text fallback** — Postgres `tsvector` / `tsquery`
- **Payload filtering** — narrow by `library`, `version`, `component` before searching
- **Embedding dimensions** — 512–768 recommended to keep RAM usage low

---

### REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/search` | Hybrid search across chunks |
| `GET` | `/api/v1/libraries` | List all indexed libraries |
| `GET` | `/api/v1/libraries/{id}` | Library detail + crawl status |
| `POST` | `/api/v1/libraries` | Register a new library source |
| `DELETE` | `/api/v1/libraries/{id}` | Remove a library |
| `GET` | `/api/v1/components/{name}` | All chunks for a component |
| `GET` | `/api/v1/chunks/{id}` | Single chunk detail |
| `POST` | `/api/v1/crawl/{libraryId}` | Trigger manual re-crawl |
| `GET` | `/api/v1/crawl/{jobId}/status` | Crawl job status |
| `POST` | `/api/v1/apikeys` | Create API key |
| `DELETE` | `/api/v1/apikeys/{id}` | Revoke API key |

---

### MCP server

Wraps search as MCP tools so AI agents discover and call Doxen natively without custom integration:

- `search_docs` — semantic search across all indexed libraries
- `get_component` — fetch all docs for a specific component by name

Exposed as an SSE endpoint compatible with Claude, GPT, and any MCP-capable agent.

---

### Project structure

```
doxen-backend/
├── src/main/java/dev/doxen/
│   ├── DoxenApplication.java
│   ├── config/
│   │   ├── QdrantConfig.java
│   │   ├── RedisConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── EmbeddingConfig.java
│   │   └── OpenApiConfig.java
│   ├── crawler/
│   │   ├── CrawlerService.java
│   │   ├── SourceAdapterFactory.java
│   │   ├── adapter/
│   │   │   ├── SourceAdapter.java        (interface)
│   │   │   ├── MarkdownAdapter.java
│   │   │   ├── HtmlCrawlerAdapter.java
│   │   │   ├── NpmAdapter.java
│   │   │   └── MavenAdapter.java
│   │   └── scheduler/
│   │       ├── CrawlScheduler.java
│   │       └── CrawlJobProducer.java
│   ├── ingestion/
│   │   ├── IngestionPipeline.java
│   │   ├── parser/
│   │   │   ├── DocumentParser.java       (interface)
│   │   │   ├── MarkdownParser.java
│   │   │   └── HtmlParser.java
│   │   ├── chunker/
│   │   │   ├── ChunkingStrategy.java     (interface)
│   │   │   ├── HeadingChunker.java
│   │   │   └── SlidingWindowChunker.java
│   │   └── embedder/
│   │       ├── EmbeddingService.java     (interface)
│   │       └── SpringAiEmbedder.java
│   ├── domain/
│   │   ├── Library.java
│   │   ├── LibraryVersion.java
│   │   ├── DocChunk.java
│   │   ├── CrawlJob.java
│   │   └── ApiKey.java
│   ├── repository/
│   │   ├── LibraryRepository.java
│   │   ├── DocChunkRepository.java
│   │   ├── CrawlJobRepository.java
│   │   ├── ApiKeyRepository.java
│   │   └── QdrantRepository.java
│   ├── search/
│   │   ├── SearchService.java
│   │   ├── VectorSearchService.java
│   │   ├── FullTextSearchService.java
│   │   └── RrfRanker.java
│   ├── api/
│   │   ├── v1/
│   │   │   ├── SearchController.java
│   │   │   ├── LibraryController.java
│   │   │   ├── ChunkController.java
│   │   │   ├── CrawlController.java
│   │   │   └── ApiKeyController.java
│   │   ├── dto/
│   │   │   ├── SearchRequest.java
│   │   │   ├── SearchResponse.java
│   │   │   ├── ChunkDto.java
│   │   │   ├── LibraryDto.java
│   │   │   └── CrawlJobDto.java
│   │   └── mapper/
│   │       ├── ChunkMapper.java
│   │       └── LibraryMapper.java
│   ├── mcp/
│   │   ├── McpController.java
│   │   ├── McpToolRegistry.java
│   │   └── tools/
│   │       ├── SearchDocsTool.java
│   │       └── GetComponentTool.java
│   ├── security/
│   │   ├── ApiKeyAuthFilter.java
│   │   ├── ApiKeyValidator.java
│   │   └── RateLimitService.java
│   └── common/
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── LibraryNotFoundException.java
│       │   └── CrawlException.java
│       └── util/
│           ├── SlugUtils.java
│           └── TokenCounter.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_libraries.sql
│       ├── V2__create_doc_chunks.sql
│       ├── V3__create_crawl_jobs.sql
│       └── V4__create_api_keys.sql
├── src/test/java/dev/doxen/
│   ├── crawler/
│   ├── ingestion/
│   ├── search/
│   └── api/
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

### Docker Compose (local dev)

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: doxen
      POSTGRES_USER: doxen
      POSTGRES_PASSWORD: secret
    ports: ["5432:5432"]
    volumes: ["pg_data:/var/lib/postgresql/data"]

  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes: ["qdrant_data:/qdrant/storage"]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

volumes:
  pg_data:
  qdrant_data:
```

---

### Suggested build order

**Phase 1 — foundation**
Get the project running: Spring Boot app, Flyway migrations, Postgres connected, Docker Compose up.

**Phase 2 — ingestion MVP**
Markdown file upload → parse → chunk by heading → store in Postgres. No embeddings yet.

**Phase 3 — search MVP**
Add Postgres full-text search (`tsvector`). Expose `GET /api/v1/search`. Test with a real library.

**Phase 4 — vector search**
Plug in Ollama locally for embeddings. Store vectors in Qdrant. Switch search to hybrid RRF.

**Phase 5 — crawlers**
Add HTML crawler (Jsoup) and npm adapter. Schedule re-crawls via Redis queue.

**Phase 6 — MCP + API keys**
Add API key auth, rate limiting, and the MCP server endpoint.

**Phase 7 — React UI**
Dashboard, library management, search playground, crawl status.
