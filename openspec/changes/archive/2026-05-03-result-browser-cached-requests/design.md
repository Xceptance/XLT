## Context

XLT provides a Result Browser that visualizes the execution history of a test case. Currently, the `CachingHttpWebConnection` intercepts requests for static content that are already in its cache. If a cache hit occurs, it serves the response directly without making a network request, but it does NOT log this activity. As a result, cached requests are invisible in the Result Browser, which differs from standard browser developer tools and makes it harder to debug caching behavior.

## Goals / Non-Goals

**Goals:**
- Record browser-cached requests (cache hits) so they appear in the result browser.
- Visually distinguish cached requests from regular requests in the result browser UI.
- Provide an easy way to enable/disable this feature via configuration.
- **TDD (Test-Driven Development)**: All new functionality must be driven by tests. Write unit and integration tests before implementing the engine and dump changes.
- Provide merge rule support (`{cache}` placeholder, `cachedPattern` / `cachedPattern.exclude`) so users can filter or group cached requests in reports as needed.
- Prevent disk space explosion by not dumping redundant response payloads for cache hits.

**Non-Goals:**
- We will not change how the caching mechanism itself works; we are only changing how cache hits are reported.

## Decisions

**1. How to capture cache hits:**
When `CachingHttpWebConnection` serves a response directly from the cache, we pass the cached status up to `XltHttpWebConnection` so it logs a `RequestData` object with a `cached` flag.

**2. Result Browser Storage & Disk Space:**
`DumpMgr` MUST NOT dump the response body for requests marked `cached=true`.
*Rationale*: Caching the same static file 1,000 times during a test run would duplicate the file in the `responses/` directory 1,000 times, causing massive disk space bloat. To fix this, we skip writing the file.

**3. Report Generator — Merge Rule Integration:**
The Report Generator includes cached requests in all aggregates by default. Users can filter them using the merge rule system via `cachedPattern` and `cachedPattern.exclude` properties, or use the `{cache}` placeholder in naming rules.
*Rationale*: A hardcoded filter would remove user control. The merge rule approach is consistent with how XLT handles all other request dimensions (URL, status code, HTTP method) and lets users decide whether to include, exclude, or separately group cached requests in their reports.

**4. Configuration:**
Introduce `com.xceptance.xlt.http.cachedRequests.logging`, defaulting to `true`.

**5. Result Browser UI updates:**
The `resultbrowser.js` will apply a specific CSS class (`contentTypeCached`) and append " (from cache)". Because the response file isn't dumped (Decision 2), the UI must intercept clicks on cached requests and show a message rather than making a failing AJAX call.

## Risks / Trade-offs

- **Risk: Backward compatibility.** 
  → **Mitigation**: Using TDD to verify the `RequestData` CSV parser handles the new boolean flag with older XLT data formats.
