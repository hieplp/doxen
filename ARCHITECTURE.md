# Doxen Backend Architecture

Canonical backend structure for `doxen-service`.

Root package: `dev.hieplp.doxen`

---

## Architecture style

**Clean + Hexagonal (Ports & Adapters)**

| Concept | Source | This project |
|---|---|---|
| Rings (Entities → Use Cases → Interface Adapters → Frameworks) | Clean Architecture | `domain` → `application` → `adapter` → `config` |
| Ports | Hexagonal | `application/port/**` — interfaces the app defines |
| Driving adapters (in) | Hexagonal | `adapter/in/` — call into the application |
| Driven adapters (out) | Hexagonal | `adapter/out/` — called by the application via ports |

---

## Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25 + Spring Boot 4 |
| Build | Gradle Kotlin DSL |
| Web | Spring MVC |
| AI / embeddings | Spring AI 2.x |
| Security | Spring Security, API key auth, rate limiting |
| Queue/cache | Redis |
| Database | PostgreSQL 16 |
| Vector index | Qdrant |
| Migrations | Flyway |
| Mapping | MapStruct |
| API docs | Springdoc OpenAPI |

---

## Dependency rule

```text
adapter/in  →  application  →  domain
adapter/out →  application ports + domain
```

- `domain` imports nothing — no Spring, no JPA, no HTTP.
- `application` owns use cases and port interfaces.
- `adapter/in` (REST, MCP, security) calls application services only.
- `adapter/out` (persistence, vector, queue, etc.) implements application ports.
- `config` wires everything together via Spring beans.

---

## Package layout

```text
src/main/java/dev/hieplp/doxen/
├── DoxenApplication.java
│
├── domain/                              # Clean: Entities ring — pure business objects
│   ├── model/
│   │   ├── Library.java
│   │   ├── LibraryVersion.java
│   │   ├── DocChunk.java
│   │   ├── CrawlJob.java
│   │   └── ApiKey.java
│   ├── enums/
│   │   ├── SourceType.java
│   │   ├── CrawlStatus.java
│   │   └── ApiKeyStatus.java
│   └── event/
│       ├── LibraryRegisteredEvent.java
│       └── CrawlCompletedEvent.java
│
├── application/                         # Clean: Use Cases ring
│   ├── port/                            # Hexagonal: outbound ports (what the app needs)
│   │   ├── repository/
│   │   │   ├── LibraryRepository.java
│   │   │   ├── LibraryVersionRepository.java
│   │   │   ├── DocChunkRepository.java
│   │   │   ├── CrawlJobRepository.java
│   │   │   └── ApiKeyRepository.java
│   │   ├── search/
│   │   │   ├── VectorSearchPort.java
│   │   │   └── FullTextSearchPort.java
│   │   ├── ingestion/
│   │   │   ├── SourceAdapter.java
│   │   │   ├── DocumentParser.java
│   │   │   └── ChunkingStrategy.java
│   │   ├── embedding/
│   │   │   └── EmbeddingPort.java
│   │   └── queue/
│   │       └── CrawlQueuePort.java
│   │
│   ├── library/
│   │   ├── LibraryService.java
│   │   └── LibraryCommandService.java
│   ├── crawl/
│   │   ├── CrawlService.java
│   │   ├── CrawlJobService.java
│   │   └── CrawlScheduler.java
│   ├── ingestion/
│   │   ├── IngestionPipeline.java
│   │   ├── ParsedDocument.java
│   │   └── ParsedChunk.java
│   ├── search/
│   │   ├── SearchService.java
│   │   ├── HybridSearchService.java
│   │   └── RrfRanker.java
│   └── apikey/
│       ├── ApiKeyService.java
│       └── ApiKeyGenerator.java
│
├── adapter/                             # Clean: Interface Adapters ring
│   │
│   ├── in/                              # Hexagonal: driving adapters (they call the app)
│   │   ├── api/
│   │   │   ├── v1/
│   │   │   │   ├── SearchController.java
│   │   │   │   ├── LibraryController.java
│   │   │   │   ├── ChunkController.java
│   │   │   │   ├── CrawlController.java
│   │   │   │   └── ApiKeyController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── SearchRequest.java
│   │   │   │   │   ├── CreateLibraryRequest.java
│   │   │   │   │   └── CreateApiKeyRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── SearchResponse.java
│   │   │   │       ├── LibraryResponse.java
│   │   │   │       ├── ChunkResponse.java
│   │   │   │       └── CrawlJobResponse.java
│   │   │   └── mapper/
│   │   │       ├── LibraryMapper.java
│   │   │       ├── ChunkMapper.java
│   │   │       └── CrawlJobMapper.java
│   │   ├── mcp/
│   │   │   ├── McpController.java
│   │   │   ├── McpToolRegistry.java
│   │   │   └── tools/
│   │   │       ├── SearchDocsTool.java
│   │   │       └── GetComponentTool.java
│   │   └── security/
│   │       ├── ApiKeyAuthFilter.java
│   │       ├── ApiKeyValidator.java
│   │       ├── RateLimitService.java
│   │       └── SecurityConstants.java
│   │
│   └── out/                             # Hexagonal: driven adapters (app calls them via ports)
│       ├── persistence/
│       │   ├── jpa/
│       │   │   ├── LibraryJpaRepository.java
│       │   │   ├── LibraryVersionJpaRepository.java
│       │   │   ├── DocChunkJpaRepository.java
│       │   │   ├── CrawlJobJpaRepository.java
│       │   │   └── ApiKeyJpaRepository.java
│       │   └── adapter/
│       │       ├── LibraryRepositoryAdapter.java
│       │       ├── LibraryVersionRepositoryAdapter.java
│       │       ├── DocChunkRepositoryAdapter.java
│       │       ├── CrawlJobRepositoryAdapter.java
│       │       └── ApiKeyRepositoryAdapter.java
│       ├── search/
│       │   └── PostgresFullTextSearchAdapter.java
│       ├── vector/
│       │   ├── QdrantVectorSearchAdapter.java
│       │   └── VectorSearchResult.java
│       ├── embedding/
│       │   └── SpringAiEmbeddingAdapter.java
│       ├── parser/
│       │   ├── MarkdownDocumentParser.java
│       │   └── HtmlDocumentParser.java
│       ├── chunker/
│       │   ├── HeadingChunkingStrategy.java
│       │   └── SlidingWindowChunkingStrategy.java
│       ├── source/
│       │   ├── SourceAdapterFactory.java
│       │   ├── MarkdownSourceAdapter.java
│       │   ├── HtmlSourceAdapter.java
│       │   ├── NpmSourceAdapter.java
│       │   └── MavenSourceAdapter.java
│       └── queue/
│           ├── RedisCrawlQueueAdapter.java
│           ├── CrawlJobProducer.java
│           └── CrawlJobConsumer.java
│
├── config/                              # Clean: Frameworks & Drivers ring — Spring wiring only
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── QdrantConfig.java
│   ├── EmbeddingConfig.java
│   ├── OpenApiConfig.java
│   └── WebConfig.java
│
└── common/
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── NotFoundException.java
    │   ├── CrawlException.java
    │   └── ValidationException.java
    └── util/
        ├── SlugUtils.java
        ├── HashUtils.java
        └── TokenCounter.java
```

---

## Resources

```text
src/main/resources/
├── application.yaml
├── application-dev.yaml
├── application-test.yaml
├── application-prod.yaml
└── db/migration/
    ├── V1__create_libraries.sql
    ├── V2__create_library_versions.sql
    ├── V3__create_doc_chunks.sql
    ├── V4__create_crawl_jobs.sql
    ├── V5__create_api_keys.sql
    └── V6__create_full_text_indexes.sql
```

---

## Layer responsibilities

### `domain`
Pure business model: entities, enums, value objects, domain events. No Spring, no JPA, no HTTP.

### `application`
Use cases and orchestration. Defines port interfaces — never imports from `adapter/`.

Use cases:
- Register a library
- Trigger and track a crawl
- Run the ingestion pipeline
- Search documentation
- Create / revoke API keys

### `adapter/in` — driving adapters
Entry points that call the application:
- **`api/`** — REST controllers, DTOs, MapStruct mappers
- **`mcp/`** — MCP SSE endpoint and tool registration
- **`security/`** — API key filter, validator, rate limiter

### `adapter/out` — driven adapters
Implementations of application ports:
- **`persistence/`** — Spring Data JPA + repository adapters (Postgres)
- **`search/`** — Postgres full-text search adapter
- **`vector/`** — Qdrant vector search adapter
- **`embedding/`** — Spring AI embedding adapter
- **`parser/`** — Jsoup HTML + Flexmark Markdown parsers
- **`chunker/`** — heading and sliding-window chunking strategies
- **`source/`** — Markdown, HTML, npm, Maven source adapters
- **`queue/`** — Redis crawl job producer + consumer

### `config`
Spring `@Configuration` classes only. Wires adapters to ports via `@Bean`.

### `common`
Shared exceptions and small stateless utilities.

---

## Ingestion pipeline

```text
Raw source
  ↓
SourceAdapter (adapter/out/source)       fetch raw content
  ↓
DocumentParser (adapter/out/parser)      parse HTML/Markdown → structured doc
  ↓
ChunkingStrategy (adapter/out/chunker)   split by heading hierarchy
  ↓
EmbeddingPort (adapter/out/embedding)    generate float[] embedding
  ↓
Store
  ├── PostgreSQL  (adapter/out/persistence)   source of truth
  └── Qdrant      (adapter/out/vector)        vector search index
```

Each chunk carries: library · version · component · source URL · heading path · content hash · token count

---

## Search design

```text
application/search/SearchService
  ↓
HybridSearchService
  ├── VectorSearchPort    →  adapter/out/vector/QdrantVectorSearchAdapter
  ├── FullTextSearchPort  →  adapter/out/search/PostgresFullTextSearchAdapter
  └── RrfRanker           →  merge + re-rank
```

Filter support: library · version · component · source type

---

## API endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/search` | Hybrid search |
| `GET` | `/api/v1/libraries` | List libraries |
| `GET` | `/api/v1/libraries/{id}` | Library detail and crawl status |
| `POST` | `/api/v1/libraries` | Register library source |
| `DELETE` | `/api/v1/libraries/{id}` | Remove library |
| `GET` | `/api/v1/components/{name}` | Chunks for a component |
| `GET` | `/api/v1/chunks/{id}` | Single chunk detail |
| `POST` | `/api/v1/crawl/{libraryId}` | Trigger manual crawl |
| `GET` | `/api/v1/crawl/{jobId}/status` | Crawl job status |
| `POST` | `/api/v1/apikeys` | Create API key |
| `DELETE` | `/api/v1/apikeys/{id}` | Revoke API key |

MCP endpoint: `GET /mcp/sse`

---

## Tests

```text
src/test/java/dev/hieplp/doxen/
├── application/       unit tests — mock all ports
├── adapter/out/       integration tests — Testcontainers (Postgres, Qdrant, Redis)
├── adapter/in/        slice tests — @WebMvcTest per controller
└── integration/       full @SpringBootTest end-to-end
```

---

## Naming conventions

| Suffix | Location | Role |
|---|---|---|
| `*Controller` | `adapter/in/api/v1` | HTTP handler |
| `*Service` | `application` | Use case / business workflow |
| `*Port` | `application/port` | Interface the application requires |
| `*Adapter` | `adapter/out` | Implements an application port |
| `*JpaRepository` | `adapter/out/persistence/jpa` | Spring Data interface |
| `*Mapper` | `adapter/in/api/mapper` | MapStruct DTO mapping |
| `*Request` / `*Response` | `adapter/in/api/dto` | REST DTOs |

---

## Build order

1. Foundation: Spring Boot app, Docker Compose, Flyway, Postgres connection.
2. Domain + migrations: libraries, versions, chunks, crawl jobs, API keys.
3. Port interfaces + Postgres adapters (`adapter/out/persistence`).
4. Markdown ingestion MVP: parse → chunk → store.
5. Postgres full-text search (`adapter/out/search`).
6. Embeddings + Qdrant vector index (`adapter/out/embedding`, `adapter/out/vector`).
7. Hybrid search with RRF.
8. HTML / npm / Maven source adapters (`adapter/out/source`).
9. Redis crawl queue + scheduler (`adapter/out/queue`).
10. API key auth + rate limiting (`adapter/in/security`).
11. MCP SSE endpoint + tools (`adapter/in/mcp`).
12. Integration tests with Testcontainers.
