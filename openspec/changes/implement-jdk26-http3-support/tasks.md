## 1. Core JDK 26 HttpClient Infrastructure

- [x] 1.1 Implement `Jdk26WebConnection` base class, implementing `WebConnection`.
- [x] 1.2 Implement the lifecycle logic to ensure `HttpClient.close()` is invoked and executors terminate when the overarching `WebClient` transaction ends.
- [x] 1.3 Add standard configuration toggles in XLT to route requests to the new JDK 26 implementation when `jdk26` is requested in `XltProperties`.

## 2. DNS and Proxy Overrides (Networking)

- [ ] 2.1 Implement a custom `java.net.spi.InetAddressResolverProvider` (JEP 418) that delegates to `DnsImpl` for custom host overrides and records.
- [ ] 2.2 Wire the custom `InetAddressResolver` into the JVM/HttpClient context correctly.
- [ ] 2.3 Implement a custom `ProxySelector` that dynamically reads XLT's proxy configuration and routes the `HttpClient` through it for HTTP/HTTPS/SOCKS.

## 3. Metrics and Instrumentation

- [ ] 3.1 Hook into the `HttpClient` request execution flow to measure `Connect` time.
- [ ] 3.2 Combine `HttpResponse.BodySubscriber` hooks to accurately capture `Time To First Byte` (TTFB) and measure total transfer times and exact payload byte sizes.
- [ ] 3.3 Format and emit these timers to the main XLT measurement/timer framework, ensuring 1:1 parity with the granularity collected by OkHttp.

## 4. Test Parameterization and Coverage

- [ ] 4.1 Refactor existing core `WebClient` test harnesses to use JUnit Parameterized test runners.
- [ ] 4.2 Configure the parameterized suite to execute across all three implementations: HtmlUnit (default), OkHttp, and Jdk26HttpClient.
- [ ] 4.3 Verify all tests succeed natively under the HTTP/3 protocol implementations driven by JDK 26.
