# http3-dns-handling

## Purpose
TBD: Defines how the JDK 26 HTTP/3 client handles custom DNS lookups and web proxy integration within XLT.

## Requirements

### Requirement: Custom DNS Resolution Bridge
The system SHALL intercept and route all DNS resolutions made by the JDK 26 `HttpClient` through the XLT `DnsImpl` engine to support custom `.hosts` overrides and dynamic test-time DNS manipulation.

#### Scenario: Intercepted DNS Lookup
- **WHEN** the `HttpClient` attempts to open a socket to a hostname
- **THEN** it first resolves the IP dynamically via `DnsImpl` instead of falling back seamlessly to OS or generic JVM DNS

### Requirement: Web Proxy Integration
The system MUST ensure that all configured XLT web proxies (HTTP/HTTPS/SOCKS) are honored by the JDK 26 `HttpClient` adapter.

#### Scenario: Proxied Request Routing
- **WHEN** general XLT properties or the test profile indicate a proxy server `proxy.host` and `proxy.port`
- **THEN** the JDK 26 `HttpClient` routes all outbound requests through that proxy, matching the behavior of OkHttp and HtmlUnit
