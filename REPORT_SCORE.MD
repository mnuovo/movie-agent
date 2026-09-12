# Project Audit Report and Improvement Scorecard

Date: 2026-09-12
Project: `movie-agent`

## Scoring Summary

| Category                   | Score (0-10) | Notes                                                                       |
|----------------------------|-------------:|-----------------------------------------------------------------------------|
| Code Complexity            |            8 | Improved by reducing duplicated mapping logic in the media mapper           |
| Clean Code                 |            8 | Strong naming/docs and cleaner mapper internals after helper extraction     |
| Performance                |            7 | Reactive search flow is now non-blocking; timeouts/resilience still pending |
| Java/Spring Best Practices |            8 | Improved with correlation logging and centralized exception handling        |
| **Overall**                |            8 | Major reliability gaps are closed; timeouts/resilience remain               |

## Top Findings (Ordered by Priority)

### 1) Critical - Remove blocking calls in reactive flow ✅
- **Severity:** High
- **Why it matters:** `.block()` in WebFlux paths can tie up threads and hurt throughput/latency.
- **Files:**
  - `src/main/java/com/michelenuovo/movieagent/tool/strategy/AutoMediaSearchStrategy.java`
  - `src/main/java/com/michelenuovo/movieagent/tool/strategy/MovieMediaSearchStrategy.java`
  - `src/main/java/com/michelenuovo/movieagent/tool/strategy/TvMediaSearchStrategy.java`
  - `src/main/java/com/michelenuovo/movieagent/tool/strategy/PersonMediaSearchStrategy.java`
  - `src/main/java/com/michelenuovo/movieagent/tool/strategy/KeywordMediaSearchStrategy.java`
- **Implemented:** Refactored the strategy/tool flow to compose `Mono`/`Flux` end-to-end, removed all production `.block()` calls from `src/main/java`, and verified the full test suite passes.

### 2) High - Add WebClient timeout configuration
- **Severity:** High
- **Why it matters:** Prevents long hangs on slow/unresponsive upstream TMDB calls.
- **File:** `src/main/java/com/michelenuovo/movieagent/client/tmdb/TmdbClient.java`
- **Action:** Configure connect/response/read timeouts at client level.

### 3) High - Add consistent logging and request correlation ✅
- **Why it matters:** Improves production troubleshooting and observability.
- **Scope:** `src/main/java/com/michelenuovo/movieagent/**`
- **Implemented:** SLF4J logs at controller boundaries, TMDB client calls, and error paths; correlation id strategy with `X-Correlation-Id`, Reactor context propagation, MDC integration, generated id when missing, and response header echo.

### 4) High - Introduce global exception handling ✅
- **Severity:** Medium
- **Why it matters:** Ensures stable, consistent API error responses.
- **Scope:** `src/main/java/com/michelenuovo/movieagent/**`
- **Implemented:** Added centralized `@RestControllerAdvice` with mapped status codes and `ApiErrorResponse` payloads via `src/main/java/com/michelenuovo/movieagent/advice/error/GlobalExceptionHandler.java`.

### 5) Medium - Improve resilience for TMDB calls
- **Severity:** Medium
- **Why it matters:** Handles transient failures more gracefully.
- **File:** `src/main/java/com/michelenuovo/movieagent/client/tmdb/TmdbClient.java`
- **Action:** Add retry/backoff and optionally circuit breaker (for external API operations).

### 6) Medium - Reduce mapper duplication ✅
- **Severity:** Medium
- **Why it matters:** Lowers maintenance cost and potential mapping drift.
- **File:** `src/main/java/com/michelenuovo/movieagent/tool/mapper/MediaSearchMapper.java`
- **Implemented:** Extracted shared helpers for response wrapping and media-result construction (`mapResponse(...)`, `mediaResult(...)`, `toStringOrNull(...)`) and replaced repeated constructor boilerplate across movie/TV/person/keyword mappings.

### 7) Medium-Low - Expand test coverage around strategies and error paths
- **Severity:** Low-Medium
- **Why it matters:** Prevents regressions during reactive refactor.
- **Scope:** `src/test/java/com/michelenuovo/movieagent/tool/strategy/**`
- **Action:** Add unit tests for each strategy, plus timeout/error/retry behavior tests.

## Priority Improvement Plan

## Phase 1 (Week 1-2): Reliability and Performance Baseline
1. [x] Eliminate `.block()` usage from strategy flow.
2. Configure WebClient timeouts.
3. [x] Add global exception handling.

**Definition of Done:**
- No blocking in request path.
- TMDB calls fail fast with explicit timeout errors.
- Consistent API error payloads across controllers.

## Phase 2 (Week 2-4): Observability and Resilience
1. [x] Add structured logging and correlation ids.
2. Add retry/backoff for TMDB operations.
3. Add basic metrics (if available in current stack).

**Definition of Done:**
- Requests are traceable in logs end-to-end.
- Transient upstream failures are retried safely.
- Error rates and latency are measurable.

## Phase 3 (Week 4-6): Maintainability and Test Confidence
1. [x] Refactor `MediaSearchMapper` duplication.
2. Increase unit and integration coverage for strategy and failure scenarios.
3. Standardize nullability/validation conventions.

**Definition of Done:**
- Reduced repetitive mapping logic.
- Clear safety net for refactors.
- More consistent code conventions.

## Strengths to Preserve
- Strong architecture and package separation (`controller`, `tool`, `strategy`, `client`).
- Good documentation and naming consistency.
- Practical strategy-pattern approach for media type routing.
- Clear externalized configuration and TMDB/Ollama integration model.

## Suggested Backlog Tickets (Ready to Create)
1. `refactor/reactive-remove-blocking-in-strategies`
2. `infra/webclient-timeouts-and-http-config`
3. `api/global-rest-exception-handler`
4. `observability/add-slf4j-logging-and-correlation-id`
5. `resilience/tmdb-retry-backoff`
6. `refactor/media-search-mapper-deduplication`
7. `test/add-strategy-unit-tests-and-error-paths`

## Next Review Checkpoint
- Re-run this scorecard after Phase 1 and target:
  - Performance: 7 -> 8
  - Best Practices: 8 -> 8+
  - Overall: 8 -> 8+
