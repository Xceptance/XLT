# http3-measurement

## Purpose
TBD: Defines how the JDK 26 HTTP/3 client is instrumented to collect highly granular web request telemetry inside XLT.

## Requirements

### Requirement: Measurement Granularity Parity
The system MUST capture and report request telemetry using the JDK 26 `HttpClient` that maps 1:1 with existing timing points found in the OkHttp implementation (Connect time, TLS handshake, TTFB, and Transfer).

#### Scenario: Valid Request Timing
- **WHEN** a request successfully completes via HTTP/3
- **THEN** the timer metrics exactly log the socket connection phase separate from the Time-To-First-Byte phase

### Requirement: Functional Testing Coverage
The system SHALL have its entire suite of core web-testing integration tests parameterized to run concurrently across HtmlUnit, OkHttp, and the new JDK 26 engines.

#### Scenario: Identical Test Success
- **WHEN** the parameterized test suite is executed
- **THEN** all tests relying on web support succeed identically regardless of which underlying engine handles the request
