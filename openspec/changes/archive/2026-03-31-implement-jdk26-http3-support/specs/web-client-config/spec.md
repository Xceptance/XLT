## MODIFIED Requirements

### Requirement: Connection Configuration Factory
The system MUST read `XltProperties` to determine which HTTP engine to instantiate when a `WebClient` or sub-system requires network access, and appropriately instantiate the `jdk26-http-client` when configured.

#### Scenario: Instantiate JDK 26 Client Engine
- **WHEN** the property `com.xceptance.xlt.http.client` is set to `jdk` (or exactly matching the convention we use for `okhttp`)
- **THEN** the `XltWebClient` delegates underlying connections to the native `java.net.http.HttpClient` wrapper
