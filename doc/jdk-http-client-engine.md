# JDK HTTP Client Engine

## Overview

XLT includes an alternative HTTP client engine based on Java's built-in `java.net.http.HttpClient` (introduced in Java 11). This engine provides native HTTP/2 support without requiring any third-party HTTP client library for core transport, and positions XLT to leverage future JDK networking improvements, including potential HTTP/3 support in upcoming Java releases.

The JDK engine is implemented in `JdkWebConnection` and integrates with HtmlUnit via the same `AbstractWebConnection` template used by the default Apache and OkHttp3 engines, ensuring feature parity for standard HTTP operations.

## Configuration

### Activating the JDK Engine

Set the following property in your XLT configuration (`project.properties`, `test.properties`, or `dev.properties`):

```properties
com.xceptance.xlt.http.client = jdk
```

### Available Engine Values

| Value | Engine | Description |
|---|---|---|
| `httpClient` (default) | Apache HttpClient | The default engine with the most complete integration. |
| `okhttp3` | OkHttp3 | Alternative engine with HTTP/2 support via OkHttp. |
| `jdk` | JDK HttpClient | New engine using Java's built-in HTTP client. |

### Insecure SSL

The JDK engine respects the standard XLT insecure SSL setting:

```properties
com.xceptance.xlt.http.client.useInsecureSSL = true
```

When enabled, the engine disables certificate verification and hostname checking, allowing connections to servers with self-signed certificates. This is typically needed in test and staging environments.

### Proxy Configuration

Proxy settings are configured through HtmlUnit's standard `WebRequest` proxy properties and are applied at the `HttpClient` builder level. Both HTTP and SOCKS proxies are supported.

### Timeouts

The JDK engine applies timeouts on a per-request basis using the standard XLT timeout property:

```properties
com.xceptance.xlt.timeout = 30000
```

The value (in milliseconds) is applied as an overall request timeout encompassing connect, send, and response phases.

> **Important:** Unlike the Apache and OkHttp3 engines, the JDK engine does not currently set a separate TCP connect timeout at the builder level. If a target server is completely unreachable (e.g., firewalled), the connection attempt may wait for the operating system's default TCP timeout (typically 60–120 seconds on Linux) before the per-request timeout takes effect. This is a known limitation to be addressed in a future release.

### Authentication

The JDK engine integrates with HtmlUnit's `CredentialsProvider` and supports standard HTTP authentication. When a server challenges with a 401 response, XLT's configured credentials are used for authentication.

## Architecture

### Component Overview

```
┌────────────────────────────────────────────────────────┐
│                    XltWebClient                        │
│                                                        │
│  com.xceptance.xlt.http.client = jdk                   │
│           │                                            │
│           ▼                                            │
│  ┌─────────────────────────────────────────────────┐   │
│  │             XltHttpWebConnection                │   │
│  │               (instrumentation)                 │   │
│  │                    │                            │   │
│  │                    ▼                            │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │          JdkWebConnection                │   │   │
│  │  │  extends AbstractWebConnection           │   │   │
│  │  │                                          │   │   │
│  │  │  ┌────────────────────────────────────┐  │   │   │
│  │  │  │   java.net.http.HttpClient         │  │   │   │
│  │  │  │   (cached per virtual user)        │  │   │   │
│  │  │  └────────▲───────────────────────────┘  │   │   │
│  │  │           │                              │   │   │
│  │  │  ┌────────┴───────────────────────────┐  │   │   │
│  │  │  │   ExecutorService                  │  │   │   │
│  │  │  │   (custom ThreadFactory)           │  │   │   │
│  │  │  └────────────────────────────────────┘  │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘

         JVM-wide DNS SPI (META-INF/services)
┌────────────────────────────────────────────────────────┐
│        XltInetAddressResolverProvider                  │
│                                                        │
│   Intercepts InetAddress.getByName() calls             │
│   from JDK HttpClient threads and routes               │
│   them through XLT's DNS resolver.                     │
│                                                        │
│   Uses IS_JDK_THREAD ThreadLocal as guard.             │
└────────────────────────────────────────────────────────┘
```

### Key Components

| Class | Package | Purpose |
|---|---|---|
| `JdkWebConnection` | `c.x.x.engine.htmlunit.jdk` | Core engine implementation. Wraps `java.net.http.HttpClient`. |
| `XltInetAddressResolverProvider` | `c.x.x.engine.htmlunit.jdk` | JVM-wide DNS SPI hook via JEP 418. Routes JDK HttpClient DNS lookups through XLT's resolver. |
| `AbstractWebConnection` | `c.x.x.engine.htmlunit` | Shared template base class for all HTTP engines. |

### Dependencies

The JDK engine introduces one additional dependency:

| Library | Version | License | Purpose |
|---|---|---|---|
| [Methanol](https://github.com/mizosoft/methanol) | 1.7.0 | MIT | Provides `MultipartBodyPublisher` for multipart/form-data uploads. The JDK's built-in `HttpClient` does not offer native multipart support. |

## How It Works

### Request Lifecycle

1. **HttpClient creation**: On the first request, a `java.net.http.HttpClient` instance is created and cached for the virtual user's transaction lifecycle. The client is configured with proxy, SSL, authentication, and redirect settings.

2. **Request building**: The HtmlUnit `WebRequest` is converted to a JDK `HttpRequest`. Headers are mapped, with structural headers (`Host`, `Content-Length`, `Connection`) filtered out as the JDK client manages these internally.

3. **Execution**: Requests are executed synchronously via `httpClient.send()`. A custom `BodyHandler` wraps the response to track time-to-first-byte (TTFB) and bytes received.

4. **Response mapping**: The JDK `HttpResponse<InputStream>` is converted back to an HtmlUnit `WebResponse`, including status code, headers, protocol version, and response body.

5. **Cleanup**: On `close()`, the cached `HttpClient` is closed and the `ExecutorService` is shut down.

### DNS Resolution

The JDK `HttpClient` does not provide a pluggable DNS resolver interface. To ensure XLT's DNS features (caching, shuffling, custom providers) work with the JDK engine, a JVM-wide DNS Service Provider Interface (SPI) hook is used.

**How it works:**

1. `XltInetAddressResolverProvider` is registered via `META-INF/services/java.net.spi.InetAddressResolverProvider`.
2. When any code calls `InetAddress.getByName()`, the provider's resolver is invoked.
3. A `ThreadLocal<Boolean>` guard (`IS_JDK_THREAD`) determines whether the calling thread belongs to the JDK HttpClient's executor pool.
4. If yes → XLT's `XltDnsResolver` handles the resolution (with caching, metrics, and DNS overrides).
5. If no → The built-in JVM resolver handles the resolution (no XLT interference).
6. A reentrancy guard (`IN_PROGRESS`) prevents infinite recursion when XLT's own resolver internally calls `InetAddress.getByName()`.

### Redirect Handling

Automatic redirects in the JDK `HttpClient` are explicitly **disabled** (`HttpClient.Redirect.NEVER`). This is by design — HtmlUnit manages redirect following itself, including loop detection and correct POST-to-GET semantic conversion for 303 responses.

### Multipart File Uploads

Since `java.net.http.HttpClient` has no built-in multipart support, the engine uses the [Methanol](https://github.com/mizosoft/methanol) library's `MultipartBodyPublisher`. It supports:

- File-based uploads (streamed via `BodyPublishers.ofInputStream()`)
- In-memory byte array uploads
- Standard text form fields

## Behavioral Differences from Other Engines

This section documents known behavioral differences between the JDK engine and the Apache/OkHttp3 engines. These are important to understand when comparing test results across engines.

### Performance Metrics

The JDK `HttpClient` provides a high-level API that encapsulates socket-level operations. Unlike Apache HttpClient (which provides socket-level instrumentation) and OkHttp3 (which provides `EventListener`), the JDK client does not expose low-level network events. This affects the reported metrics:

| Metric | Apache | OkHttp3 | JDK | Notes |
|---|---|---|---|---|
| **DNS Time** | Real | Real | Real | JDK engine uses SPI hook for instrumentation. |
| **Connect Time** | Real | Real | Always 0ms | JDK HttpClient manages connections internally; no hook available. |
| **Bytes Sent** | Real | Real | Estimated | Approximated as URI length + 200 bytes (headers) + body size. |
| **TTFB** | Real | Real | Partially real | Measured when `BodyHandler.apply()` is called (response headers received). |
| **Bytes Received (body)** | Real | Real | Real | Accurately tracked via `BodySubscriber.onNext()`. |
| **Bytes Received (headers)** | Real | Real | Estimated | Approximated as 256 + actual header string lengths. |

**Impact on reports:**

- **Connect time** will always show as `0 ms` in XLT reports when using the JDK engine. This does not mean connections are instant — it means XLT cannot measure the connect phase separately.
- **Bandwidth metrics** (bytes sent/received) are estimates and may differ from real wire-level bytes by 5–20%, especially for requests with large cookie headers or many custom headers.
- **Overall request time** (`loadTime`) is always accurate — it measures the wall-clock time from request initiation to response completion, regardless of the engine.

> **Recommendation:** If precise network-level metrics (connect time, exact bandwidth) are critical for your analysis, use the default Apache engine or OkHttp3 engine. The JDK engine is best suited for scenarios where HTTP/2 support and modern protocol compatibility are more important than granular socket metrics.

### Protocol Version Reporting

The JDK engine maps protocol versions to standard ALPN identifiers:

| JDK Version Enum | Reported As |
|---|---|
| `HTTP_1_1` | `http/1.1` |
| `HTTP_2` | `h2` |

This is consistent with what browsers and other HTTP engines report.

### Reason Phrases

The JDK `HttpClient` does not return HTTP reason phrases (e.g., "OK", "Not Found"). The engine maps status codes to their standard English reason phrases using Apache HttpComponents' `EnglishReasonPhraseCatalog` as a fallback.

### Connection Lifecycle

The JDK `HttpClient` is created lazily on the first request and cached for the virtual user's entire transaction lifecycle. This differs from OkHttp3, which creates a new client per request (though sharing connection pools). Implications:

- **Proxy changes mid-transaction are not supported.** If the proxy configuration changes between requests within the same transaction, the JDK engine will continue using the initially configured proxy. For load tests, proxy configuration is typically global and static, so this is rarely an issue.
- **The HttpClient is closed when the `WebClient` is closed**, releasing all pooled connections and background threads.

### Cookie Management

Cookies are managed entirely by HtmlUnit — not by the JDK `HttpClient`. HtmlUnit injects `Cookie:` headers into requests and parses `Set-Cookie:` headers from responses at a higher layer. The JDK `HttpClient`'s internal cookie handler is not used.

## JVM-Wide Impact of the DNS SPI Hook

> **This section is critical for teams deploying XLT agents alongside other JVM tooling.**

The DNS resolver provider (`XltInetAddressResolverProvider`) is registered via the Java Service Provider Interface and affects the **entire JVM**, not just the JDK HTTP engine. This means:

1. **All `InetAddress.getByName()` calls pass through XLT's provider**, including those from:
   - The Apache HTTP engine (default)
   - Database drivers (JDBC)
   - Cloud SDK clients (AWS, GCP, Azure)
   - Any other library performing DNS resolution

2. **The guard mechanism ensures safety**: The provider only routes DNS through XLT's resolver when the calling thread is flagged as a JDK HttpClient thread (`IS_JDK_THREAD == true`). All other threads fall through to the standard JVM resolver with minimal overhead (two `ThreadLocal.get()` checks).

3. **Performance overhead**: Each DNS resolution incurs two `ThreadLocal.get()` calls for the guard check, regardless of the engine in use. At high throughput (50,000+ DNS resolutions/second), this is negligible but measurable in CPU profiling.

4. **The SPI is always active**: The provider is registered via `META-INF/services` and is loaded when the JVM starts, even if the JDK engine is not selected. When the JDK engine is not in use, no threads will have `IS_JDK_THREAD == true`, so the provider is effectively a no-op pass-through — but it is still on the call path.

### Recommendations for Multi-Tool Deployments

If you run XLT alongside other tools in the same JVM (e.g., monitoring agents, sidecar processes):

- Be aware that XLT has registered a global DNS resolver provider.
- The provider does not interfere with non-XLT DNS resolutions by design.
- If you encounter unexpected DNS behavior, check whether another library has also registered an `InetAddressResolverProvider`. Only one provider can be active at a time (the JVM selects the first one found).

## When to Use the JDK Engine

### Good Use Cases

- **HTTP/2 testing**: The JDK engine natively supports HTTP/2 with ALPN negotiation, making it suitable for testing modern HTTP/2 services.
- **Future-proofing**: As the JDK evolves (especially with potential HTTP/3 support in future releases), the JDK engine will benefit automatically from platform-level improvements.
- **Minimal dependency footprint**: The JDK engine relies on Java's built-in networking stack, reducing external dependency surface for core HTTP transport.
- **Protocol compatibility testing**: Validating that your application behaves correctly with Java's native HTTP client, which is increasingly used in modern Java applications.

### When to Prefer Other Engines

- **Precise network metrics are required**: If you need accurate connect time, exact bytes-on-wire counts, or granular socket-level timing, use the default Apache engine.
- **Maximum proven stability**: The Apache and OkHttp3 engines have been battle-tested in XLT across many years and customer deployments. The JDK engine is newer and may have edge cases that have not yet been encountered.
- **HTTP/1.1-only targets**: If your target application only speaks HTTP/1.1, there is no advantage to using the JDK engine.

## Troubleshooting

### Common Issues

| Symptom | Cause | Solution |
|---|---|---|
| Connect time always shows 0 ms | Expected behavior for the JDK engine. | No fix needed. See [Performance Metrics](#performance-metrics). |
| SSL handshake failures with self-signed certs | Insecure SSL not enabled. | Set `com.xceptance.xlt.http.client.useInsecureSSL = true`. |
| Long hangs before timeout on unreachable hosts | No builder-level connect timeout. | Increase awareness; will be fixed in a future release. |
| `ClassNotFoundException` for `XltInetAddressResolverProvider` | Incorrect module path or missing service file. | Verify `META-INF/services/java.net.spi.InetAddressResolverProvider` is present in the classpath. |
| DNS resolution not using XLT's custom settings | Thread not flagged as JDK thread. | This is a bug — file an issue. |

### Enabling Debug Output

The JDK `HttpClient` supports JVM-level debug logging via the system property:

```
-Djdk.httpclient.HttpClient.log=all
```

This produces verbose output of the client's internal operations, including connection pool management, header processing, and HTTP/2 frame details. Use this for diagnosing protocol-level issues.

For XLT-level logging, the standard SLF4J configuration applies. The JDK engine logs connection setup events at `DEBUG` level through XLT's logging infrastructure.

## Version History

| Version | Changes |
|---|---|
| XLT 10.x (current) | Initial release of the JDK HTTP client engine. Supports HTTP/1.1, HTTP/2, and HTTP/3 when used with JDK 26+. |
