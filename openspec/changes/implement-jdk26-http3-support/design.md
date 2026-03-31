## Context

XLT provides load testing capabilities using different HTTP engines, primarily HtmlUnit (Standard) and OkHttp (Pluggable layer). With JDK 26, the built-in `java.net.http.HttpClient` boasts mature support for HTTP/3, QUIC, and lower-latency asynchronous operations. To leverage this and provide modern alternatives, we will add a third pluggable underlying connection mechanism using this native JDK capability. This requires deep integration with XLT's core network infrastructure, specifically the measurement instrumentation and custom DNS handling (`DnsImpl`).

## Goals / Non-Goals

**Goals:**
- Provide a `jdk26-http-client` wrapper that can be toggled via existing configuration property patterns (e.g., matching the `XltProperties` usage for enabling OkHttp).
- Accurately instrument the JDK 26 HttpClient to report connect times, time-to-first-byte (TTFB), transfer times, and content sizes to the XLT metrics system.
- Implement a DNS override bridge, tying the JDK 26 HttpClient proxy/networking behavior to XLT's `DnsImpl` and custom host override rules.
- **Enhance Test Coverage:** Refactor all core web support tests to dynamically execute across all three implementations (HtmlUnit default, OkHttp, and the new JDK 26 HttpClient) to guarantee complete functional parity.

**Non-Goals:**
- Removing or heavily modifying existing HtmlUnit or OkHttp implementations; they will run side-by-side.
- Backporting this functionality to older JDKs (as it relies heavily on JDK 26 specific enhancements for robust HTTP/3).

## Decisions

- **Configuration Toggling:** We will add a new value (e.g., `jdk26`) to the property that dictates the active client engine. The loading logic will instantiate the JDK client adapter seamlessly if configured via `XltProperties`.
- **Metrics Instrumentation:** Because the `java.net.http.HttpClient` API hides much of its socket-level interactions, we will hook into the `HttpResponse.BodySubscriber` and request dispatch wrappers to precisely measure blocking time, TTFB, and exact payload bytes for the XLT timer system. 
- **DNS and Proxy Overrides:** We will implement a custom `java.net.spi.InetAddressResolverProvider` (JEP 418) that intercepts standard JVM name resolution and routes it through XLT's `DnsImpl`. This guarantees full native compatibility with JDK `HttpClient`'s TLS SNI negotiation while respecting dynamic test host mappings. We will also utilize a custom `ProxySelector` to bridge XLT's proxy configuration into the client.- **Testing Architecture:** We will convert our existing `WebClient` integration tests into JUnit `Parameterized` tests. Each test will boot with varying `XltProperties` overrides to explicitly configure each of the three client modes.

## Risks / Trade-offs

- **Risk:** JDK `HttpClient`'s high-level API obscures low-level socket timings, making it difficult to match the granularity of OkHttp's metric points.
  - **Mitigation:** We'll conduct intensive side-by-side protocol traces and explore JDK flight recorder / internal logging hooks to accurately derive TTFB and connect times.
- **Risk:** Lack of pluggable `DnsResolver` in the JDK API.
  - **Mitigation:** We'll experiment with dynamic mapping. If a custom proxy selector wrapper is insufficient, we will alter the Request URIs directly to the IP and implement custom `Host` SSL/TLS SNI hostnames manually.
- **Risk:** Resource/Thread Pool leaks or contention during high-currency load tests. `java.net.http.HttpClient` heavily relies on common or custom `Executor` pools which can become a bottleneck.
  - **Mitigation:** We will explicitly configure and manage the `Executor` caching thread pools STRICTLY per VUser/transaction, avoiding global state entirely. All resources (connections, threads) will be forcefully cleared via `HttpClient.close()` (introduced in JDK 21) or appropriate lifecycle methods during session teardown.
