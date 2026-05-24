## Why

Currently, requests answered from the browser cache are silently swallowed by XLT's `CachingHttpWebConnection` and never appear in the result browser. In the developer tools of all major browsers (Chrome, Firefox, Edge), such requests are listed with a "(from cache)" or "(disk cache)" annotation. This discrepancy makes it difficult to compare XLT result browser output with real-browser network traces, hindering debugging and validation workflows.

## What Changes

- Intercept cache hits inside `CachingHttpWebConnection` and forward them as synthetic `RequestData` entries to the session's data manager and request history, so they appear in the result browser.
- Add a `cached` flag to `RequestData` so that cached requests can be distinguished from regular server requests.
- Add a configurable property (`com.xceptance.xlt.http.cachedRequests.logging`, default: `true`) to control whether cached requests are logged.
- Extend the result browser frontend to visually distinguish cached requests (e.g., via a distinct CSS class and color) from regular requests.
- Ensure cached requests carry meaningful metadata: URL, content type, response code (200), zero timing values, and a "from cache" indicator.

## Capabilities

### New Capabilities
- `cached-request-logging`: Intercepting and recording browser-cached requests as `RequestData` entries in the XLT engine, controlled by a configuration property.
- `cached-request-display`: Displaying cached requests in the result browser frontend with a visual "from cache" indicator.

### Modified Capabilities

_(none — no existing specs to modify)_

## Impact

- **Engine layer**: `CachingHttpWebConnection` (cache-hit path), `XltHttpWebConnection` (data logging), `RequestData` (new `cached` field), `RequestHistory` / `Request` (accept cached entries).
- **Result browser frontend**: `resultbrowser.js` (rendering logic, content-type class determination), CSS styles (new `.cached` class).
- **Configuration**: New property in the `com.xceptance.xlt.http` namespace; must be documented in the default property files.
- **Data format**: The CSV timer data format for `RequestData` gains one new trailing field (`cached`), requiring backward-compatible parsing in `setRemainingValues`.
- **Report generator**: `RequestDataProcessor` and merge-rule conditions should be aware of cached entries but treat them as regular request data unless explicitly filtered.
