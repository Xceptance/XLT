## Why
JDK 26 introduces more mature HTTP/3 support via its built-in HTTP Client API. Adding pluggable HTTP/3 support using the JDK 26 API will allow XLT load tests to evaluate modern web service architectures, leveraging the performance and latency benefits of QUIC and HTTP/3. This capability complements our existing HTTP/1.1 and HTTP/2 testing capabilities provided by HtmlUnit and OkHttp, and will be structured similarly to our current OkHttp pluggable layer.

## What Changes
We will introduce a new web connection implementation built on the `java.net.http.HttpClient` API, sitting alongside the existing `OkHttp3WebConnection` and standard HtmlUnit. This alternative HTTP client mode will be fully pluggable through existing XLT configurations. We will explicitly tackle the challenges of instrumenting the JDK 26 client for XLT's measurement endpoints (capturing request metrics) and integrating XLT's custom DNS handling logic.

## Capabilities

### New Capabilities
- `jdk26-http-client`: Core abstraction integrating the JDK 26 HttpClient into the XLT WebClient pipeline.
- `http3-measurement`: Hooking into the JDK 26 HttpClient to accurately capture timers (connect, TTFB, transfer) and request/response metrics for XLT reporting.
- `http3-dns-handling`: Implementing custom DNS resolution and host override logic compatible with the JDK 26 HttpClient system.

### Modified Capabilities
- `web-client-config`: Add properties to configure and enable the JDK 26 HTTP/3 client (e.g. extending our existing connection config toggles). Ensure that the property handling for configuration strictly matches what we already do (e.g., using `XltProperties` patterns as seen in OkHttp configurations).

## Impact
- **XLT Web Client:** `XltWebClient` and connection handling factories will be modified to support routing through the new JDK connection layer when configured.
- **Metrics/Reporting System:** The new client needs to carefully hook into the XLT timer and request data collection systems, ensuring feature parity with the OkHttp implementation.
- **DNS Subsystem:** XLT's internal `DnsImpl` or equivalent resolvers will need a bridge to the JDK's DNS/Proxy routing API.
