# Code Review: Cached Browser Request Logging (617) — Round 2

**Branch:** `617-cached-requests` (based on `origin/develop`)  
**Scope:** 30 modified files, ~303 insertions / ~29 deletions + 5 new files  
**Review Date:** 2026-05-02  

---

## 1. Feature Summary

The change adds the ability for XLT to **log browser-cached requests** (requests served from `CachingHttpWebConnection`'s in-memory cache without hitting the network). Previously these were invisible. Now:

1. A `cached` boolean is added to `RequestData` and serialized to CSV.
2. Cache hits are intercepted via a `reportCacheHit()` template method hook in `CachingHttpWebConnection`, overridden in `XltHttpWebConnection`.
3. A new `{cache}` placeholder and `cachedPattern` / `cachedPattern.exclude` are added to the merge rule system.
4. The result browser and HAR export show cache status visually.
5. Logging is controlled by `com.xceptance.xlt.http.cachedRequests.logging` (default: `true`).

---

## 2. Architecture Assessment

### 2.1 Template Method Hook — Good ✅

The `CachingHttpWebConnection.reportCacheHit()` → `XltHttpWebConnection.reportCacheHit()` override is a clean, non-invasive extension. It keeps `CachingHttpWebConnection` unaware of XLT's logging/session machinery. The empty default in the base class is the right pattern.

### 2.2 Data Model Extension — Good ✅

Adding `cached` to `RequestData` with backward-compatible CSV parsing (`if values.size() > 24`) follows the established versioning pattern (similar to `usedIpAddress` at index 23). The version comment now correctly reads `// XLT 10.0.0`.

### 2.3 MergeRule Integration — Well Structured ✅

The `CachedCondition` / `CachedEmptyCondition` pair follows the same pattern as every other condition type (e.g., `HttpMethodCondition`, `StatusCodeCondition`). The type code `"cache"` and placeholder `{cache}` are consistent.

The `PLACEHOLDER_PATTERN` regex was extended to `([acmnrstu]|cache)` — this is the first multi-character code. This works but is a structural shift worth noting for future extensibility.

### 2.4 Result Browser Integration — Good ✅

- The **CSS class `.contentTypeCached`** is correctly applied to left navigation request entries via `resultbrowser.js` (line 403), greying out and italicizing cached requests for visual distinction.
- The **status line** appends `" (from cache)"` text.
- The **response body tab** shows `"Response body not stored for cached requests"` instead of trying to load missing content.

---

## 3. Issues Addressed (Round 1 → Round 2)

### 3.1 ~~Property Read on Every Cache Hit~~ — Fixed ✅

The `logCachedRequests` property is now read once in the `static {}` initializer block, consistent with all other properties in `XltHttpWebConnection`. The field is `private static` (non-final to allow test toggling via reflection).

### 3.2 ~~Version Comment~~ — Fixed ✅

Changed from `// XLT cache hit feature` to `// XLT 10.0.0`.

### 3.3 ~~RequestDataProcessor.processDataRecord Reorder~~ — Reverted ✅

The reorder had no functional effect. Reverted to original call order (`super.processDataRecord(data)` first).

### 3.4 ~~`webResponse.getRawSize()` Defensiveness~~ — Fixed ✅

Now uses `Math.max(0, webResponse.getRawSize())` to guard against negative/unexpected values from cached `WebResponse` objects.

### 3.5 ~~Missing NetworkData Logging~~ — Documented ✅

Added explicit comment:
```java
// Note: NetworkData is intentionally NOT logged here. Cache hits do not involve
// any actual network traffic, so they should not appear in the NetworkDataManager.
```

### 3.6 ~~DumpMgr Redundant `fileName = null`~~ — Fixed ✅

Replaced the redundant `fileName = null` branch with cleaner positive-guard logic:
```java
if (request.requestData == null || !request.requestData.isCached())
{
    // dump content...
}
```

### 3.7 ~~RequestInfo.cached No Javadoc~~ — Fixed ✅

Added Javadoc: `/** Whether the request was served from the browser cache. */`

### 3.8 ~~Version Constant `7_40_00`~~ — Fixed ✅

Changed to `10_00_00` throughout. Test methods and helpers renamed from `XLT_7_4_0` to `XLT_10_0_0`.

### 3.9 ~~pom.xml Version Bump~~ — Acknowledged, Ignored ✅

The version bump to `10.0.0-beta-10` is for local testing only.

### 3.10 ~~IDE Config Files~~ — Acknowledged ✅

`.classpath`, `.project`, `.settings` diffs are IDE noise and won't be committed with the feature.

### 3.11 ~~Missing `this.`~~ — No remaining violations found ✅

Reviewed the new code — `this.` is used correctly in `reportCacheHit()` for `this.putAdditionalRequestData(...)` and `this.cloneWebRequest(...)`. No violations remain in the new code.

### 3.12 ~~CSS Class `.contentTypeCached`~~ — Not Dead Code ✅

The class is correctly applied to the left navigation's request list entries (line 403 in `resultbrowser.js`). It greys out and italicizes cached requests in the request tree.

---

## 4. Remaining Items

### Still Worth Checking 💭

| # | Item | Severity |
|---|---|---|
| 1 | `PLACEHOLDER_PATTERN` mixes single-char and multi-char codes (`[acmnrstu]|cache`) — future extensibility pattern worth tracking | Low |
| 2 | No test for `HarExporter` cache marking (`HarCache`/`HarCacheRequest`) | Medium |
| 3 | `CachedRequestsLoggingTest.testDumpMgrSkipsCachedPayloads` asserts 1 file but doesn't verify it's the *right* file | Low |

---

## 5. Test Results

All 60 tests pass after fixes:

| Test Class | Tests | Result |
|---|---|---|
| `CachedRequestsLoggingTest` | 3 | ✅ Pass |
| `MergeRule_Cached_Test` | 3 | ✅ Pass |
| `MergeRuleProcessorTest` | 12 | ✅ Pass |
| `RequestDataProcessorTest` | 1 | ✅ Pass |
| `RequestDataTest` | 35 (1 skipped) | ✅ Pass |
| `RequestDataMgrTest` | 6 | ✅ Pass |
| **Total** | **60** | **BUILD SUCCESS** |

---

## 6. Overall Verdict

The feature is **architecturally sound and now polished**. All must-fix and should-fix items from the first review have been addressed. The remaining items are low-severity tracking notes.
