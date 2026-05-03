## 1. Engine Core & Data Model (TDD)

- [x] 1.1 **Test:** Write tests verifying `RequestData` handles a new boolean `cached` flag in `toList` and `setRemainingValues` (forward/backward compatibility).
- [x] 1.2 **Impl:** Add `cached` boolean flag to `com.xceptance.xlt.api.engine.RequestData`.
- [x] 1.3 **Impl:** Update `RequestData.toList()` and `RequestData.setRemainingValues()` to process the `cached` flag.
- [x] 1.4 **Impl:** Add configuration property `com.xceptance.xlt.http.cachedRequests.logging` to `default.properties` (default: `true`).

## 2. Request Interception & Logging (TDD)

- [x] 2.1 **Test:** Write integration tests verifying `CachingHttpWebConnection` cache hits are logged when enabled, and ignored when disabled.
- [x] 2.2 **Impl:** Update `XltHttpWebConnection` and `CachingHttpWebConnection` to track and log cache hits with `cached = true` and `0ms` network timings.
- [x] 2.3 **Impl:** Respect the `com.xceptance.xlt.http.cachedRequests.logging` property.

## 3. Data Generation & DumpMgr (TDD)

- [x] 3.1 **Test:** Write tests verifying `RequestDataMgr` copies the `cached` flag to `RequestInfo`.
- [x] 3.2 **Test:** Write tests verifying `DumpMgr` skips writing the response body to `responses/` for requests with `cached=true`.
- [x] 3.3 **Impl:** Update `RequestInfo` to include `cached` flag and copy it in `RequestDataMgr.getRequestInfo(...)`.
- [x] 3.4 **Impl:** Update `DumpMgr` to skip the response body dump if `cached=true`.

## 4. Report Generator Accuracy (TDD)

- [x] 4.1 **Test:** Write tests verifying `RequestDataProcessor` or `DataParserThread` ignores `RequestData` entries with `cached=true`.
- [x] 4.2 **Impl:** Update the Report Generator parsing/processing logic to exclude cached requests from performance metrics aggregates.

## 5. Result Browser UI

- [x] 5.1 Modify `resultbrowser.js` to add `.contentTypeCached` class if `cached` is true.
- [x] 5.2 Modify `resultbrowser.js` to append " (from cache)" to the status or size indicator.
- [x] 5.3 Update `resultbrowser.js` Request Content tab to show "Response body not stored for cached requests" instead of failing an AJAX request for missing dumps.
- [x] 5.4 Update CSS files to style `.contentTypeCached` (e.g., light grey/italicized).
- [x] 5.5 Ensure the HAR export handles cached requests correctly.
