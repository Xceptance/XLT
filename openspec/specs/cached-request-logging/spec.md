## Purpose

Intercepting and recording browser-cached requests as `RequestData` entries in the XLT engine, with configurable logging and merge rule support in the Report Generator.

## Requirements

### Requirement: Log Cached Requests
The XLT engine SHALL intercept cache hits during HTTP request processing and log them as `RequestData` entries, so they are available to the Result Browser.

#### Scenario: Request served from cache
- **WHEN** a resource is served directly from the internal cache
- **THEN** the system logs a `RequestData` entry for this request
- **AND** the `RequestData` entry is marked as `cached = true`
- **AND** the `RequestData` entry has network timing metrics set to 0

### Requirement: Configurable Cache Logging
The logging of cached requests SHALL be configurable via a property.

#### Scenario: Cache logging is disabled
- **WHEN** the property `com.xceptance.xlt.http.cachedRequests.logging` is set to `false`
- **AND** a resource is served from the cache
- **THEN** no `RequestData` entry is logged

#### Scenario: Cache logging is enabled
- **WHEN** the property `com.xceptance.xlt.http.cachedRequests.logging` is set to `true`
- **AND** a resource is served from the cache
- **THEN** a `RequestData` entry is logged

### Requirement: Report Generator Merge Rule Support
The XLT Report Generator SHALL support filtering cached requests via the existing merge rule system, using a `{cache}` placeholder and `cachedPattern` / `cachedPattern.exclude` properties. Cached requests are included in aggregates by default, giving users full control over how they are handled.

#### Scenario: Filtering cached requests via merge rules
- **WHEN** the Report Generator processes `timers.csv`
- **AND** a merge rule is configured with `cachedPattern.exclude = true`
- **THEN** requests where `cached = true` are excluded from that rule's aggregation

#### Scenario: No cache filter configured
- **WHEN** the Report Generator processes `timers.csv`
- **AND** no `cachedPattern` merge rule is configured
- **THEN** cached requests are included in aggregates like any other request (with their 0ms timings)
