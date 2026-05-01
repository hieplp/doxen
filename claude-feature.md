# Doxen — Use Cases

**Doxen** is a documentation crawler and knowledge API for AI agents. It ingests library documentation from multiple sources, stores it as structured searchable chunks, and exposes REST and MCP interfaces so AI agents and developers can query documentation on demand.

---

## Core Domain Model

| Entity | Description |
|---|---|
| **Library** | A logical package/project, e.g. `react`, `spring-ai`, or an internal UI kit |
| **Library Version** | A version of a library; one version can be marked as default |
| **Documentation Source** | A crawlable source for a library version, such as a docs site, local directory, package registry artifact, generated Javadoc/JSDoc, or uploaded `.zip` |
| **Crawl Job** | A queued/running/completed/failed indexing execution for one documentation source |
| **Chunk** | A structured section of documentation with content, heading path, metadata, full-text index data, and vector embedding |
| **API Key** | A hashed credential with scopes, expiry, and rate-limit settings |

---

## Global API Rules

- All non-public API routes require an API key in the `Authorization: Bearer <key>` header.
- API keys are validated by hash and identified by an 8-character stored prefix.
- Rate limits are enforced per API key by default. The default limit is `60 req/min` unless overridden on the key.
- Public routes may include health checks, OpenAPI documentation, and the MCP discovery endpoint if explicitly configured.
- Deleted libraries are hidden from search and scheduled crawls by default.

---

## Actors

| Actor | Description |
|---|---|
| **Admin** | Manages libraries, API keys, crawl configuration, and deletion/revocation operations |
| **AI Agent** | Queries documentation via REST API or MCP protocol |
| **Developer** | Uses the React dashboard and search playground |
| **System** | Automated scheduler and workers that trigger and execute periodic re-crawls |

---

## UC-01: Register a Library

**Actor:** Admin  
**Goal:** Add a new documentation source so its content can be indexed and searched.

**Main flow:**
1. Admin sends `POST /api/v1/libraries` with library name, slug, default version, source type, and source URL/path/package name.
2. System validates the payload, slug uniqueness, source type, and version metadata.
3. System creates a `Library`, default `LibraryVersion`, and initial `DocumentationSource` record in Postgres.
4. System associates an embedding configuration with the source, including provider, model, dimensions, and Qdrant collection.
5. System returns the created library ID, version ID, and source ID.

**Extensions:**
- Additional versions can be registered per library; one version is marked as default.
- Supported source types include local file/directory, HTML docs site, npm package, Maven package, JSDoc/Javadoc output, Markdown/MDX, or `.zip` bulk upload.

---

## UC-02: Ingest Documentation

**Actor:** System  
**Goal:** Fetch, parse, chunk, embed, and store documentation content.

**Main flow:**
1. System starts a crawl job for a documentation source.
2. System fetches content from the source using the configured connector: URL crawl via Jsoup, local file read, package registry API, or uploaded archive extraction.
3. System applies include/exclude patterns and crawl depth limits.
4. System parses content using Jsoup for HTML and Flexmark for Markdown/MDX.
5. System splits content into structured chunks by heading hierarchy.
6. System extracts metadata such as source URL/path, title, heading path, component name, library, version, and content hash.
7. System generates dense vector embeddings via Spring AI using OpenAI, Cohere, Ollama, or another configured provider.
8. System stores chunk metadata, full content, content hash, and full-text index data in Postgres.
9. System stores vectors and searchable payload filters in Qdrant.
10. System marks stale chunks from previous successful crawls as inactive or deleted according to the source retention policy.

**Failure handling:**
- Crawl jobs are idempotent by source ID and job ID.
- Failed fetches, parse errors, and embedding errors are recorded on the crawl job.
- Workers retry transient failures using the configured retry policy.
- A partially failed crawl does not replace the previous successful index unless enough chunks are successfully processed according to configured thresholds.

---

## UC-03: Trigger a Manual Crawl

**Actor:** Admin / Developer  
**Goal:** Force an immediate re-index of a library, version, or documentation source.

**Main flow:**
1. Caller sends `POST /api/v1/libraries/{libraryId}/crawl` with optional `versionId` or `sourceId`.
2. System validates permissions and confirms the target library/source is active.
3. System enqueues a crawl job in the Redis queue.
4. System returns a `jobId` for status tracking.
5. System worker picks up the job and runs UC-02.

---

## UC-04: Schedule Automatic Re-crawls

**Actor:** System  
**Goal:** Keep indexed documentation up to date without manual intervention.

**Main flow:**
1. System scheduler reads all enabled documentation sources where `last_crawled_at + crawl_interval_mins <= now`.
2. System skips disabled, deleted, or already-running sources.
3. System enqueues one crawl job per due source in the Redis queue.
4. System worker processes each job via UC-02.

**Extensions:**
- Per-source include/exclude URL patterns and crawl depth limits control what gets re-crawled.
- Scheduler may apply jitter or concurrency limits to avoid overloading source sites and embedding providers.

---

## UC-05: Check Crawl Job Status

**Actor:** Admin / Developer  
**Goal:** Monitor the progress of an in-flight or completed crawl.

**Main flow:**
1. Caller sends `GET /api/v1/crawl-jobs/{jobId}`.
2. System returns status: `queued`, `running`, `completed`, `failed`, or `cancelled`.
3. Response includes timestamps, source ID, library ID, version ID, processed chunk count, failed item count, retry count, and error details when available.

---

## UC-06: Search Documentation

**Actor:** AI Agent / Developer  
**Goal:** Find relevant documentation chunks for a query.

**Main flow:**
1. Caller sends `GET /api/v1/search?q=<query>` or `POST /api/v1/search` for longer queries and complex filters.
2. Optional filters include `library`, `version`, `component`, `source_type`, `source_id`, and `limit`.
3. System generates an embedding for the query.
4. System runs vector search on Qdrant using semantic similarity and applies payload filters before vector search when possible.
5. System runs full-text search on Postgres using `tsvector` / `tsquery`.
6. System merges and re-ranks results using Reciprocal Rank Fusion (RRF).
7. System returns ranked chunks with IDs, scores, content snippets, source metadata, heading paths, library/version data, component names, and retrieval metadata.

---

## UC-07: Retrieve a Component's Documentation

**Actor:** AI Agent / Developer  
**Goal:** Get all indexed chunks for a specific library component.

**Main flow:**
1. Caller sends `GET /api/v1/libraries/{librarySlug}/versions/{version}/components/{componentName}`.
2. If `version` is omitted by using `GET /api/v1/libraries/{librarySlug}/components/{componentName}`, System uses the library's default version.
3. System retrieves active chunks associated with that component name, ordered by heading path and source order.
4. System returns chunks with content, metadata, heading paths, source URLs/paths, and chunk IDs.

---

## UC-08: Retrieve a Single Chunk

**Actor:** AI Agent / Developer  
**Goal:** Fetch the full content and metadata of one documentation chunk.

**Main flow:**
1. Caller sends `GET /api/v1/chunks/{id}`.
2. System validates access and confirms the chunk is active.
3. System returns chunk content, heading path, library, version, component, source metadata, content hash, and timestamps.

---

## UC-09: Query via MCP Protocol

**Actor:** AI Agent  
**Goal:** Query Doxen directly from within an AI agent's tool-use loop without calling REST manually.

**Main flow:**
1. AI Agent connects to the configured MCP transport, such as `GET /mcp/sse` for SSE or a streamable HTTP endpoint if enabled.
2. Agent authenticates using an API key if the MCP endpoint is protected.
3. Agent discovers registered MCP tools, including `SearchDocsTool`, `GetComponentTool`, and `GetChunkTool`.
4. Agent invokes a tool with a structured schema.
5. Doxen executes UC-06, UC-07, or UC-08 internally.
6. Doxen returns structured results through the MCP transport.

---

## UC-10: Create an API Key

**Actor:** Admin  
**Goal:** Grant a client authenticated access to the API.

**Main flow:**
1. Admin sends `POST /api/v1/apikeys` with a name, scopes, rate limit, and optional expiry.
2. System generates a key, stores the hash and 8-character prefix, and returns the plain-text key once.
3. System records scopes such as `libraries:read`, `libraries:write`, `crawl:write`, `search:read`, and `apikeys:write`.
4. All subsequent protected API requests must include the key in the `Authorization` header.
5. System validates the key, expiry, revocation status, scopes, and rate limit on every request.

---

## UC-11: Revoke an API Key

**Actor:** Admin  
**Goal:** Immediately invalidate a compromised or expired key.

**Main flow:**
1. Admin sends `DELETE /api/v1/apikeys/{id}`.
2. System marks the key as revoked.
3. All further requests using that key are rejected.

---

## UC-12: Browse Libraries via Dashboard

**Actor:** Developer  
**Goal:** View, register, update, and remove libraries through a UI without using the REST API directly.

**Main flow:**
1. Developer opens the React dashboard.
2. Dashboard calls `GET /api/v1/libraries` and displays the library list using TanStack Table.
3. Developer can view library details via `GET /api/v1/libraries/{id}`.
4. Developer can register a new library using UC-01.
5. Developer can update source configuration using `PATCH /api/v1/libraries/{id}` or version/source-specific update routes.
6. Developer can remove an existing library using `DELETE /api/v1/libraries/{id}`.
7. Developer can see latest crawl status per library inline.

---

## UC-13: Use the Search Playground

**Actor:** Developer  
**Goal:** Interactively test search queries and inspect results before integrating.

**Main flow:**
1. Developer opens the Search Playground in the dashboard.
2. Developer types a query and optionally sets filters such as library, version, component, and source type.
3. Dashboard calls `GET /api/v1/search` for simple queries or `POST /api/v1/search` for complex requests.
4. Dashboard renders ranked chunks with RRF score, vector/full-text rank metadata, content snippets, and source metadata.

---

## UC-14: View Crawl Analytics

**Actor:** Admin / Developer  
**Goal:** Monitor indexing health and crawl throughput over time.

**Main flow:**
1. Developer opens the Analytics view in the dashboard.
2. Dashboard fetches crawl stats and job history using `GET /api/v1/crawl-jobs` and analytics summary endpoints.
3. Dashboard renders charts using Recharts showing job history, chunk counts, error rates, crawl duration, and embedding usage.

---

## UC-15: Remove a Library

**Actor:** Admin  
**Goal:** Decommission a library and stop future crawls.

**Main flow:**
1. Admin sends `DELETE /api/v1/libraries/{id}` or removes it via the dashboard.
2. System soft-deletes the library record by setting `deleted_at`.
3. System disables associated versions and documentation sources.
4. Scheduled crawls for this library no longer trigger.
5. Search and component retrieval exclude the library's chunks by default.
6. Depending on retention policy, System either keeps chunks/vectors hidden for audit/history or enqueues asynchronous cleanup for Postgres chunks and Qdrant vectors.

---

## UC-16: List Libraries

**Actor:** Admin / Developer / AI Agent  
**Goal:** Discover available indexed libraries.

**Main flow:**
1. Caller sends `GET /api/v1/libraries`.
2. System returns active libraries with slug, name, default version, available versions, latest crawl status, and chunk counts.

---

## UC-17: Get Library Details

**Actor:** Admin / Developer / AI Agent  
**Goal:** Inspect a library's versions, sources, and indexing status.

**Main flow:**
1. Caller sends `GET /api/v1/libraries/{id}` or `GET /api/v1/libraries/by-slug/{slug}`.
2. System returns library metadata, versions, documentation sources, crawl configuration, latest crawl jobs, and indexing statistics.

---

## UC-18: Update Library or Source Configuration

**Actor:** Admin  
**Goal:** Modify library metadata, version defaults, source settings, or crawl configuration.

**Main flow:**
1. Admin sends `PATCH /api/v1/libraries/{id}` for library-level fields or a source/version-specific update route for nested settings.
2. System validates changes, permissions, and source configuration.
3. System persists the updates.
4. If indexing-relevant fields changed, System can optionally enqueue a crawl job.

---

## UC-19: List Crawl Jobs

**Actor:** Admin / Developer  
**Goal:** Review crawl history and troubleshoot indexing issues.

**Main flow:**
1. Caller sends `GET /api/v1/crawl-jobs` with optional filters such as `libraryId`, `sourceId`, `status`, and date range.
2. System returns paginated crawl jobs with status, timestamps, counts, retry information, and error summaries.

---

## UC-20: Health Check

**Actor:** Admin / System  
**Goal:** Verify that Doxen and its dependencies are operational.

**Main flow:**
1. Caller sends `GET /health` or `GET /api/v1/health`.
2. System checks application status, Postgres connectivity, Redis connectivity, Qdrant connectivity, and embedding provider availability when configured.
3. System returns overall status and dependency-level details.
