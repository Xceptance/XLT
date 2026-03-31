# jdk26-http-client

## Purpose
TBD: Defines the architectural and resource-management behaviors of the JDK 26 HTTP/3 connection layer in XLT.

## Requirements

### Requirement: Pluggable JDK 26 HTTP Client Architecture
The system SHALL provide a pluggable implementation of the `WebConnection` interface using the `java.net.http.HttpClient` API, alongside the existing HtmlUnit and OkHttp implementations.

#### Scenario: Instantiation via Configuration
- **WHEN** the XLT configuration property is set to use the `jdk26` engine
- **THEN** an instance of the newly created JDK 26-backed connection class is instantiated and used for all requests within that WebClient instance

### Requirement: Resource Management and Leak Prevention
The system MUST rigorously manage thread pools, connections, and system resources attached to instances of the `java.net.http.HttpClient` to prevent leaks or contention during high-currency test executions.

#### Scenario: Forceful Teardown on Session Close
- **WHEN** the simulated WebClient session completes or closes
- **THEN** the system invokes `HttpClient.close()` and forcefully terminates internal executor pools, ensuring a 0-resource leak footprint
- **THEN** all open sockets or HTTP/3 QUIC streams associated with that client are forcefully terminated

### Requirement: Per-Transaction Client Lifecycle
The system MUST instantiate and maintain the `java.net.http.HttpClient` on a per-`WebClient` (per-transaction) basis heavily avoiding global connection pooling.

#### Scenario: Transaction-scoped Isolated Instances
- **WHEN** a new transaction or `WebClient` initializes
- **THEN** a completely isolated `HttpClient` instance (and its associated executor/resources) is created exclusively for that transaction lifecycle, leaving no global state behind
