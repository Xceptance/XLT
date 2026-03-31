# Code Review: JDK HTTP Client Engine Implementation

**Branch:** `implement-jdk26-http3-support`  
**Compared to:** `develop`  
**Reviewer Roles:** Performance Engineer, Security Reviewer, API & Design Reviewer, Correctness & Concurrency Reviewer, Test Quality Reviewer, Coding Standards Reviewer, QA Tester, Grumpy OG Developer

---

## Files Reviewed

| File | Type |
|---|---|
| [JdkWebConnection.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java) | New — Core HTTP engine |
| [XltInetAddressResolverProvider.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java) | New — JVM-wide DNS SPI hook |
| [XltWebClient.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/XltWebClient.java) | Modified — Integration point |
| [pom.xml](file:///home/rschwietzke/projects/GIT/XLT.xslt/pom.xml) | Modified — New dependency |
| [NOTICE.md](file:///home/rschwietzke/projects/GIT/XLT.xslt/NOTICE.md) | Modified — License |
| [JdkWebConnectionTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionTest.java) | New — Unit test |
| [JdkWebConnectionInsecureSslTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionInsecureSslTest.java) | New — SSL integration test |
| [XltInetAddressResolverProviderTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProviderTest.java) | New — DNS resolver test |
| [MultipartFileUploadTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/MultipartFileUploadTest.java) | New — Multipart integration test |
| [DeleteRequestWithBodyTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/DeleteRequestWithBodyTest.java) | Modified — Parameterized for `jdk` |
| [XltWebClientTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/XltWebClientTest.java) | Modified — Parameterized for `jdk` |
| [XltWebClientLoadStaticContentTest.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/XltWebClientLoadStaticContentTest.java) | Modified — Parameterized for `jdk` |
| [SPI config](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/resources/META-INF/services/java.net.spi.InetAddressResolverProvider) | New — ServiceLoader registration |

---

## Role 1: Performance Engineer 🏎️

This is the most critical lens for a **performance testing tool**. Every microsecond and every allocation counts when running at thousands of virtual users.

---

### 🔴 P1 — `readAllBytes()` Buffers the Entire Response in Memory

[JdkWebConnection.java:531](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L531)

```java
byte[] bytes = responseBody.readAllBytes();
```

**Problem:** `readAllBytes()` materializes the *entire* response body into a single `byte[]` before the `WebResponseData` is constructed. For large responses (multi-MB HTML pages, large JSON API payloads, downloadable files), this creates momentary heap spikes that are double the response size (one in the JDK's internal buffer, one in the returned array). Under load with hundreds of concurrent virtual users, this can cause significant GC pressure and even `OutOfMemoryError`.

**Comparison with OkHttp3:** The OkHttp3 implementation uses `responseBody.bytes()`, which has the same issue, so this is *consistent* behavior. However, the Apache implementation uses stream-based reading.

**Suggestion:** This is acceptable for now since it mirrors OkHttp3's behavior. However, for future optimization, consider using `InputStream` directly with `WebResponseData(InputStream, ...)` or a chunked reader to avoid the full buffering. Document this as a known limitation/future optimization opportunity.

---

### 🟡 P2 — `Executors.newCachedThreadPool()` Has Unbounded Thread Growth

[JdkWebConnection.java:136](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L136)

```java
this.executor = Executors.newCachedThreadPool(new ThreadFactory() { ... });
```

**Problem:** `newCachedThreadPool()` creates an unbounded pool — threads are created on demand with no upper limit. Under burst traffic patterns (many rapid concurrent requests per virtual user), this can spawn hundreds of threads *per virtual user*, causing extreme context-switch overhead.

**Comparison with OkHttp3:** OkHttp uses a `Dispatcher` with explicit `maxRequests(64)` and `maxRequestsPerHost(6)` limits. The Apache client uses a fixed connection pool.

**Suggestion:** Consider using `Executors.newFixedThreadPool(N)` or, since this is per-virtual-user, a very tight pool:

```java
this.executor = Executors.newFixedThreadPool(
    Math.min(6, Runtime.getRuntime().availableProcessors()),
    threadFactory
);
```

Alternatively, use virtual threads via `Executors.newVirtualThreadPerTaskExecutor()` when virtual threads are enabled in XLT configuration (there's already a `useVirtualThreads` property in `XltWebClient`).

---

### 🟡 P3 — Heuristic `approxRequestBytes` Can Mislead Reports at Scale

[JdkWebConnection.java:421](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L421)

```java
long approxRequestBytes = request.uri().toString().length() + 200L; // Headers + URI
```

**Problem:** The hardcoded `200L` for header approximation and `256` for response header approximation (line 447) are rough estimates. In reality, requests with many cookies, authorization tokens, or correlation IDs can have headers exceeding 2–4KB. The reported "bytes sent" and "bytes received" metrics will be systematically under-counted, potentially misleading performance analysis.

**Mitigation applied (good):** The `WARNING` comments at lines 411-414 and 418-420 clearly document that these are heuristic approximations. This is honest and appropriate.

**Suggestion:** Consider computing actual header sizes from `webRequest.getAdditionalHeaders()` for the request side:
```java
long headerBytes = 0;
for (Map.Entry<String, String> h : webRequest.getAdditionalHeaders().entrySet())
{
    headerBytes += h.getKey().length() + h.getValue().length() + 4; // ": " + "\r\n"
}
```
This would be much more accurate with negligible overhead.

---

### 🟡 P4 — `XltDnsResolver` Instantiated Per-Resolution in the SPI Hook

[XltInetAddressResolverProvider.java:74](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java#L74)

```java
XltDnsResolver xltDnsResolver = new XltDnsResolver();
```

**Problem:** A new `XltDnsResolver` is created for every single DNS lookup. Looking at `XltDnsResolver`'s constructor, it reads from `XltProperties.getInstance()`, creates a `DnsOverrideResolver`, and potentially a `DnsJavaHostNameResolver`. This is significant overhead per resolution.

**Comparison with OkHttp3:** The OkHttp3 implementation creates a single `DnsImpl(new XltDnsResolver())` in the constructor, shared across all lookups for that connection's lifetime.

**Suggestion:** Cache the `XltDnsResolver` instance, either per-thread or as a lazily initialized field. Since `XltDnsResolver` stores a per-user cache via `addressesByHostName`, it could be stored in a `ThreadLocal`:

```java
private static final ThreadLocal<XltDnsResolver> RESOLVER_CACHE = new ThreadLocal<>();
```

Or simply instantiate once in the `XltInetAddressResolver` constructor and reuse.

---

### 🟢 P5 — Connect Time Is Always 0ms (Documented But Important)

[JdkWebConnection.java:411-416](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L411)

```java
socketMonitor.connectingStarted();
socketMonitor.connected();
```

**Assessment:** This is well-documented with an honest `WARNING` comment. Connect time will always be 0ms in reports. This is a fundamental limitation of `java.net.http.HttpClient`'s opaque connection pool.

**Impact:** Users comparing JDK engine reports against Apache/OkHttp3 will see a discrepancy. This should be documented in the user-facing documentation/changelog.

---

### 🟢 P6 — Static `INSECURE_SSL_CONTEXT` Initialization (Good)

[JdkWebConnection.java:82-103](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L82)

**Assessment:** The insecure SSL context is correctly initialized once in a static initializer block and shared across all instances. This is efficient and mirrors OkHttp3's `INSECURE_SSL_SOCKET_FACTORY` pattern.

---

## Role 2: Security Reviewer 🔒

---

### 🟢 S1 — Insecure SSL Properly Guarded by Configuration

[JdkWebConnection.java:221-225](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L221)

```java
if (webClient.getOptions().isUseInsecureSSL())
{
    builder.sslContext(INSECURE_SSL_CONTEXT);
    builder.sslParameters(INSECURE_SSL_PARAMETERS);
}
```

**Assessment:** Insecure SSL is only applied when explicitly enabled via configuration. The `INSECURE_SSL_PARAMETERS` correctly disables endpoint identification (`setEndpointIdentificationAlgorithm("")`), which is required for self-signed certs. This is consistent with the OkHttp3 approach using `EasyHostnameVerifier`.

---

### 🟡 S2 — Credentials Provider Scoped Broadly

[JdkWebConnection.java:199-206](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L199)

```java
Credentials creds = credentialsProvider.getCredentials(expectedScope);
if (creds == null)
{
    creds = credentialsProvider.getCredentials(AuthScope.ANY);
}
```

**Problem:** Falling back to `AuthScope.ANY` means that if precise scope matching fails, *any* stored credentials may be sent to *any* server. This is documented as "matching XLT's legacy behavior" in the comment, which is acceptable context.

**Assessment:** This is a known pre-existing pattern, not introduced by this change. The comment at line 197–198 explains the rationale. No action needed, but worth a note if security-conscious users ask.

---

### 🟢 S3 — SPI Registration is JVM-wide (Correctly Documented)

The `META-INF/services/java.net.spi.InetAddressResolverProvider` file registers `XltInetAddressResolverProvider` globally for the JVM. The `IS_JDK_THREAD` guard in the resolver correctly scopes XLT DNS behavior to only threads managed by the JDK HTTP client, preventing data leakage or unexpected behavior in non-XLT contexts.

---

## Role 3: API & Design Reviewer 🏗️

---

### 🟢 D1 — Clean Integration Into `AbstractWebConnection` Template

The `JdkWebConnection` cleanly extends `AbstractWebConnection<HttpClient, HttpRequest, HttpResponse<InputStream>>`, implementing all required template methods. This is a well-structured application of the template method pattern.

---

### 🟡 D2 — `HttpClient` Created Lazily but Configuration Captured Too Early

[JdkWebConnection.java:134-231](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L134)

**Problem:** The `createHttpClient()` method checks `if (this.httpClient == null)` and caches the client for the transaction lifecycle. However, proxy configuration and SSL settings are read from the *first* `WebRequest` that triggers creation. If a subsequent request in the same transaction changes proxy settings (e.g., via `webRequest.setProxyHost()`), those changes will be silently ignored.

**Comparison with OkHttp3:** The OkHttp3 implementation creates a *new* `OkHttpClient` for every `createHttpClient()` call (no caching), meaning it respects per-request proxy/SSL changes. This is a *behavioral difference*.

**Suggestion:** Either:
1. Document that the JDK engine doesn't support per-request proxy changes (acceptable for a load testing tool where proxy config is typically global), or
2. Invalidate the cached client when proxy/SSL settings change between requests.

---

### 🟡 D3 — `IS_JDK_THREAD` ThreadLocal Is `public static` — Broad Visibility

[JdkWebConnection.java:107](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L107)

```java
public static final ThreadLocal<Boolean> IS_JDK_THREAD = ThreadLocal.withInitial(() -> false);
```

**Problem:** This is `public` because `XltInetAddressResolverProvider` (in the same package) needs to read it. However, `public static` exposes it to any class in the project, which is a wider surface than necessary.

**Suggestion:** Since both classes are in the same package (`com.xceptance.xlt.engine.htmlunit.jdk`), this could be package-private:
```java
static final ThreadLocal<Boolean> IS_JDK_THREAD = ThreadLocal.withInitial(() -> false);
```

---

### 🟡 D4 — Missing `@Override` on `close()` Method

[JdkWebConnection.java:237](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L237)

The `close()` method does not have proper `@Override` Javadoc like the other overridden methods. It has `{@inheritDoc}` but check if the parent interface `WebConnection` actually declares `close()`. Looking at the OkHttp3 connection, its `close()` throws `IOException`. The JDK version's `close()` doesn't throw `IOException` — verify this matches the contract.

---

### 🟢 D5 — Protocol Version Mapping Is Correct

[JdkWebConnection.java:544-556](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L544)

The switch statement correctly maps `HTTP_1_1` → `"http/1.1"` and `HTTP_2` → `"h2"`, with a fallback to `response.version().name()`. This matches the ALPN protocol identifiers used by browsers and the OkHttp3 engine.

**Note on test:** The `testMakeWebResponseMapping` test at line 104 asserts `"HTTP_2"` as the protocol version, but the actual production code would return `"h2"` for `HTTP_2`. This is because the mock response is processed through `makeWebResponseTesting()` which calls the real `makeWebResponse()`. Let me look more carefully... The test creates a `MockHttpResponse` with `HTTP_2` version, and the real code maps that to `"h2"`. But the assertion is:

```java
Assert.assertEquals("HTTP_2", webResponse.getProtocolVersion());
```

> [!CAUTION]
> **This test assertion appears incorrect.** The production code at line 551 maps `HTTP_2` to `"h2"`, but the test at line 104 expects `"HTTP_2"`. Either the test is wrong, or the production code's mapping is never reached during test execution. **This needs investigation** — it may indicate the mock is returning a different version, or the test is not actually exercising the switch statement. See [test line 104](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionTest.java#L104).

**Wait** — re-reading the `switch`: It uses `response.version()` which returns `HttpClient.Version.HTTP_2`. The `case HTTP_2:` branch sets `"h2"`. But the mock indeed sets the version as `HttpClient.Version.HTTP_2`. So the test *should* expect `"h2"`, not `"HTTP_2"`. **This is a bug in the test.** If this test passes, it may be because the `@SuppressWarnings("deprecation")` annotation is hiding something, or the test was never run successfully.

---

### 🟡 D6 — Reason Phrase Catalog Uses Apache HTTP Components

[JdkWebConnection.java:574](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L574)

```java
String reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
```

**Problem:** This imports `org.apache.http.impl.EnglishReasonPhraseCatalog` from Apache HttpComponents — creating a cross-module dependency. The JDK engine ideally shouldn't depend on Apache HTTP client internals.

**Suggestion:** Create a simple private static method or a small utility map with the ~30 most common HTTP reason phrases, eliminating the Apache dependency for this file. For example:

```java
private static final Map<Integer, String> REASON_PHRASES = Map.ofEntries(
    Map.entry(200, "OK"),
    Map.entry(301, "Moved Permanently"),
    Map.entry(302, "Found"),
    Map.entry(304, "Not Modified"),
    Map.entry(400, "Bad Request"),
    Map.entry(404, "Not Found"),
    Map.entry(500, "Internal Server Error")
    // ... etc.
);
```

However, since Apache HttpComponents is already a transitive dependency via HtmlUnit, this is acceptable as-is and doesn't add any new JAR. It's more of a cleanliness concern.

---

## Role 4: Correctness & Concurrency Reviewer 🧵

---

### 🔴 C1 — `IS_JDK_THREAD` Set Redundantly in Both `executeRequest()` and `ThreadFactory`

[JdkWebConnection.java:142-152](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L142) and [JdkWebConnection.java:508](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L508)

**Problem:** `IS_JDK_THREAD` is set to `true` in the `ThreadFactory` wrapper (for threads spawned by the HttpClient's internal pool) **and** in `executeRequest()` (for the calling thread). This dual-marking has subtly different semantics:

1. The **ThreadFactory** marks threads that the HttpClient *creates internally* (e.g., for reading, writing, H2 frame processing).
2. The **executeRequest()** marks the *calling* thread (the virtual user's thread) while it is blocked on `httpClient.send()`.

The `executeRequest()` usage is correct because `httpClient.send()` is a *synchronous* call that can trigger DNS resolution on the calling thread before dispatching to the pool.

However, when `IS_JDK_THREAD.remove()` is called in the `finally` block of `executeRequest()`, it clears the flag for the calling thread, which is correct. But the ThreadFactory threads will have `IS_JDK_THREAD` permanently set to `true` for their entire lifetime. If those threads are reused by the `CachedThreadPool` for *other* purposes (e.g., completable futures, parallel streams), they'd still have `IS_JDK_THREAD == true`, potentially causing unexpected DNS hook activations.

**Suggestion:** This is probably fine because the `CachedThreadPool` is *exclusively* used by this `HttpClient`, so the threads won't be shared. But add a comment explaining why both locations are necessary.

---

### 🟡 C2 — `createHttpClient()` is Not Thread-Safe

[JdkWebConnection.java:134](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L134)

```java
if (this.httpClient == null)
{
    // ... create ...
    this.httpClient = builder.build();
}
return this.httpClient;
```

**Problem:** This is not synchronized. If `getResponse()` is called concurrently from multiple threads (e.g., via the `RequestQueue` for parallel static content downloads), there's a race where multiple `HttpClient` instances could be created — with only the last one surviving and the earlier ones leaked (along with their `ExecutorService` threads).

**Comparison with OkHttp3:** OkHttp3 does not have this problem because it creates a new client per call, and `OkHttpClient` instances are immutable and lightweight (they share the connection pool).

**Suggestion:** Either:
1. Synchronize the creation block, or
2. Create the `HttpClient` eagerly in the constructor, or
3. Use `volatile` + double-checked locking:

```java
private volatile HttpClient httpClient;
// ...
if (this.httpClient == null)
{
    synchronized (this)
    {
        if (this.httpClient == null)
        {
            // build it
        }
    }
}
```

**Note:** In practice, XLT's `AbstractWebConnection.getResponse()` is likely called sequentially from the virtual user's main thread, and static content downloads go through the `RequestQueue` which uses its own `WebClient` copies. So this may be safe in practice, but it's architecturally fragile.

---

### 🟡 C3 — `IN_PROGRESS` ThreadLocal in Resolver Prevents Reentrancy, but...

[XltInetAddressResolverProvider.java:53](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java#L53)

```java
private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> false);
```

**Assessment:** The reentrancy guard is well-designed. If `XltDnsResolver` internally triggers another DNS lookup (e.g., `PlatformHostNameResolver` calling `InetAddress.getAllByName()`), the `IN_PROGRESS` flag prevents infinite recursion by falling back to the built-in resolver. This is correct and necessary.

---

### 🟡 C4 — Silent Fallback to Builtin Resolver on Exception

[XltInetAddressResolverProvider.java:85](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java#L85)

```java
return builtin.lookupByName(host, lookupPolicy);
```

**Problem:** If `XltDnsResolver.resolve()` throws any exception *other* than `UnknownHostException`, the code silently falls back to the built-in resolver. This masks errors and makes debugging difficult. For example, an NPE from `RequestExecutionContext.getCurrent()` returning null would be silently swallowed, and DNS resolution would work — but without XLT's DNS timing instrumentation.

**Suggestion:** At minimum, log the exception before falling back:
```java
catch (Exception e)
{
    if (e instanceof UnknownHostException)
    {
        throw (UnknownHostException) e;
    }
    // Log the unexpected exception for debugging before falling back
    LOG.debug("XLT DNS resolution failed for '{}', falling back to builtin", host, e);
    return builtin.lookupByName(host, lookupPolicy);
}
```

---

### 🟢 C5 — `close()` Order is Correct

[JdkWebConnection.java:239-248](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L239)

The `close()` method correctly closes the `HttpClient` first (which stops accepting new requests) and then shuts down the `ExecutorService` (which terminates background threads). The null-checks prevent double-close issues.

---

## Role 5: Test Quality Reviewer 🧪

---

### 🔴 T1 — `testMakeWebResponseMapping` Has Incorrect Protocol Assertion

[JdkWebConnectionTest.java:104](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionTest.java#L104)

```java
Assert.assertEquals("HTTP_2", webResponse.getProtocolVersion());
```

**Problem:** The production code maps `HttpClient.Version.HTTP_2` to `"h2"` (line 551 of `JdkWebConnection`). This assertion expects `"HTTP_2"`, which would only be the fallback `default:` branch output (`response.version().name()` returns `"HTTP_2"`). This means either:

1. The `switch` statement's `case HTTP_2:` is **dead code** and never matched (possible if the Version enum changed), or
2. The test assertion is simply wrong.

**Suggestion:** Fix the assertion to `Assert.assertEquals("h2", ...)` and verify the test passes. If it doesn't, the switch statement logic needs investigating.

---

### 🟡 T2 — `testMakeWebResponseMapping` Comment is Stale

[JdkWebConnectionTest.java:105](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionTest.java#L105)

```java
// Since reason phrase mapping is not yet implemented (Issue #6), it will be empty
```

But `getReasonPhrase()` **is** implemented (lines 570–581 of JdkWebConnection) and uses `EnglishReasonPhraseCatalog`. The test at line 106 asserts `""`, but for status 200, `EnglishReasonPhraseCatalog` should return `"OK"`.

**Problem:** This means either:
1. The assertion is wrong (should be `"OK"` since reason phrase mapping IS implemented), or
2. The `getReasonPhrase()` method is not working correctly.

**Suggestion:** Fix the assertion to `Assert.assertEquals("OK", webResponse.getStatusMessage())` and remove the stale comment about "Issue #6".

---

### 🟡 T3 — `testInsecureSslRejected` Assertion Is Too Weak

[JdkWebConnectionInsecureSslTest.java:134](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnectionInsecureSslTest.java#L134)

```java
Assert.assertTrue(e.getMessage() != null || e.getCause() != null);
```

**Problem:** This asserts that `(message != null OR cause != null)` — which is true for virtually *any* exception. This assertion would pass even if the exception were completely unrelated to SSL (e.g., a `NullPointerException`).

**Suggestion:** Assert the exception type or message content:
```java
Throwable root = e;
while (root.getCause() != null) root = root.getCause();
Assert.assertTrue(
    "Expected SSL-related exception but got: " + root.getClass().getName(),
    root instanceof javax.net.ssl.SSLHandshakeException ||
    root instanceof javax.net.ssl.SSLException
);
```

---

### 🟡 T4 — `testIsolationWithNullContext` Has Empty Setup (No-Op)

[XltInetAddressResolverProviderTest.java:70-86](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/test/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProviderTest.java#L70)

```java
// Remove RequestExecutionContext entirely to simulate isolated background threads running bare
```

This comment suggests the test intends to null out `RequestExecutionContext`, but there's no code doing that (lines 75-76 are blank). The test then resolves `"localhost"`, which succeeds because the resolver likely falls back to built-in resolution.

**Problem:** The test doesn't actually test the null-context scenario. `RequestExecutionContext.getCurrent()` probably still returns a valid context.

**Suggestion:** Either implement the null-context simulation (if possible), or rename the test to reflect what it actually tests (e.g., `testResolveLocalhostWithJdkThreadFlagSet`).

---

### 🟢 T5 — Parameterized Integration Tests Are Excellent

The conversion of `XltWebClientTest`, `XltWebClientLoadStaticContentTest`, `DeleteRequestWithBodyTest`, and `MultipartFileUploadTest` to run all three engines (`default`/`apache4`, `okhttp3`, `jdk`) is thorough and ensures functional parity. This is exactly the right approach for a pluggable engine architecture.

---

### 🟡 T6 — No Test for Timeout Behavior

The `createRequest()` method sets `Duration.ofMillis(timeout)` as the request timeout when `timeout > 0`. There is no test verifying:
1. Request-level timeout is applied
2. WebClient-level fallback timeout works
3. Negative timeout means "use WebClient default"

**Suggestion:** Add a test with a slow-responding server that verifies timeout exceptions.

---

### 🟡 T7 — No Test for Proxy Configuration

The proxy configuration code (lines 166–186) is untested. Since proxy support is important for enterprise testing behind corporate proxies, this is a meaningful gap.

---

## Role 6: Coding Standards Reviewer 📏

---

### 🟡 CS1 — Duplicate Import: `java.net.ProxySelector` Imported Twice

[JdkWebConnection.java:32](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L32) and [line 38](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L38)

```java
import java.net.ProxySelector;  // line 32
import java.net.ProxySelector;  // line 38 — duplicate!
```

**Suggestion:** Remove the duplicate import at line 38.

---

### 🟡 CS2 — Imports Are Not Alphabetically Ordered

The current import block mixes `java.io`, `java.net`, `java.nio`, `java.net.http`, `com.github.mizosoft`, `com.xceptance`, `java.net` (again), `java.nio`, `java.time`, `java.util`, `org.apache`, `org.htmlunit`, `java.util.concurrent`, `javax`, `org.htmlunit` (again), `com.xceptance`, `java.security`, `com.xceptance`, `org.apache`.

**Suggestion:** Group and sort imports according to standard Java conventions:
1. `java.*`
2. `javax.*`
3. `org.*`
4. `com.*`

---

### 🟡 CS3 — Allman Style Violations in Several Places

While most of the code correctly uses Allman style, there are a few violations:

**Line 303-305:**
```java
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
```

**Line 335:**
```java
                    } catch (Exception e) {
```

These should use Allman style with braces on their own lines per `AGENTS.md`.

---

### 🟡 CS4 — `instanceof` Without Pattern Matching (Java 21)

Per `AGENTS.md`, modern Java 21 features should be preferred. Several places use traditional `instanceof` with explicit casting:

[JdkWebConnection.java:298-300](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L298):
```java
if (pair instanceof KeyDataPair)
{
    final KeyDataPair filePair = (KeyDataPair) pair;
```

[XltInetAddressResolverProvider.java:81](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java#L81):
```java
if (e instanceof UnknownHostException)
{
    throw (UnknownHostException) e;
}
```

**Suggestion:** Use pattern matching:
```java
if (pair instanceof KeyDataPair filePair)
{
    // use filePair directly
}

if (e instanceof UnknownHostException uhe)
{
    throw uhe;
}
```

---

### 🟡 CS5 — Missing Javadoc on Several Methods

The following methods lack Javadoc (AGENTS.md requires Javadoc on all methods unless overriding):

- `JdkWebConnection.java` private `createRequest()` at line 372 — **has Javadoc** ✅
- `JdkWebConnection.java` private `getReasonPhrase()` at line 570 — **has Javadoc** ✅  
- `XltInetAddressResolverProvider.java` constructor of inner class at line 55 — missing Javadoc
- `XltInetAddressResolverProvider.java` `lookupByName()` at line 61 — override, OK
- `XltInetAddressResolverProvider.java` `lookupByAddress()` at line 94 — override, OK

The inner class constructor at line 55 should have a brief `@param` doc.

---

### 🟢 CS6 — Apache License Headers Present on All New Files ✅

All new source files include the correct Apache 2.0 license header.

---

### 🟢 CS7 — NOTICE.md and 3rd-Party Licenses Correctly Updated ✅

The `methanol` dependency is properly registered in `NOTICE.md` with MIT license, and the full license text is in `doc/3rd-party-licenses/methanol/LICENSE`.

---

## Role 7: QA Tester 🧑‍🔬

*"I don't care what it's supposed to do. I care what it actually does when I torture it."*

---

### 🔴 QA1 — No Cookie Handling Whatsoever

The JDK `HttpClient` has its own cookie management (`java.net.CookieHandler`). But `JdkWebConnection` does **not** configure any cookie jar — neither disabling it nor bridging it to HtmlUnit's `CookieManager`.

**Comparison:** OkHttp3 explicitly wires `CookieJarImpl(webClient.getCookieManager())` at [OkHttp3WebConnection.java:134](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/okhttp3/OkHttp3WebConnection.java#L134). Apache does the same through its own cookie store integration.

**What will happen in production:** The JDK HttpClient's default `CookieHandler` is `null` (no cookies). Cookies are managed at the HtmlUnit `WebClient` level, which injects them as `Cookie:` headers via `finalizeRequestHeaders()` or the automatic header pipeline. So this *probably* works by accident — HtmlUnit pre-populates the `Cookie` header, and the JDK client sends it.

**But here's the risk:** If the JDK HttpClient has a non-null default `CookieHandler` (e.g., set by another library or framework), it could *silently duplicate or conflict* with HtmlUnit's cookie management. This would cause bizarre session issues under load — exactly the kind of Heisenbug that wastes days.

**Test gap:** There is **no test** that verifies cookies work correctly with the JDK engine. No login/session test, no multi-request cookie persistence test.

**Suggestion:** Explicitly disable JDK's cookie handling:
```java
builder.cookieHandler(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_NONE));
```
And add a cookie integration test.

---

### 🟡 QA2 — Multipart: Missing Edge Case for `file == null && data == null`

[JdkWebConnection.java:310-347](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L310)

The multipart logic handles `filePair.getFile() != null` and `filePair.getData() != null`, but there's no fallback `else` for when **both** are null. The OkHttp3 implementation at [line 316-318](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/okhttp3/OkHttp3WebConnection.java#L316) handles this:

```java
else
{
    builder.addFormDataPart(name, filename, RequestBody.create(new byte[0], mediaType));
}
```

**What will happen in production:** If a `KeyDataPair` has both file and data as null (e.g., an empty file upload field), the JDK implementation will **silently skip the part entirely**. The server will receive a malformed multipart body missing expected fields, leading to 400 errors or incomplete form submissions.

**Suggestion:** Add the missing `else` branch to match OkHttp3's behavior.

---

### 🟡 QA3 — `TODO: timeouts` Comment at Builder Level vs. Actual Implementation

[JdkWebConnection.java:227](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L227)

```java
// TODO: timeouts
this.httpClient = builder.build();
```

Timeout IS applied per-request in `createRequest()` at line 388–396. But the `HttpClient.Builder` itself supports `.connectTimeout()` for the *connect phase*, which is different from the per-request timeout.

**The difference:**
- **Builder-level `connectTimeout()`** — TCP connect timeout. This covers the gap identified in P5 where connect time is always 0ms.
- **Per-request `timeout()`** — Overall request timeout including connect, send, and response.

**What will happen in production:** Without a builder-level connect timeout, if a target server is completely unreachable (firewalled, dropped packets), the JDK HttpClient will hang for the *OS-level default TCP timeout* (typically 60–120 seconds on Linux!) before the per-request timeout kicks in. OkHttp3 sets connect, read, AND write timeouts at the builder level.

**Suggestion:** Add `builder.connectTimeout(Duration.ofMillis(timeout))` at the builder level, using the same XLT-configured timeout. Remove the `TODO` comment.

---

### 🟡 QA4 — No Test for Empty Request Body on POST

What happens when a POST request has no parameters and no body? Looking at `AbstractWebConnection.makeRequest()`, the logic branches at line 178:
```java
if (webRequest.getEncodingType() == FormEncodingType.URL_ENCODED && method == HttpMethod.POST)
```
With an empty parameter list and null request body, `URLEncodedUtils.format()` returns an empty string, which is passed to `createRequestWithStringBody()`. This creates a `BodyPublishers.ofByteArray(new byte[0])` — which is semantically different from `BodyPublishers.noBody()`.

**Test gap:** No test verifies that a bodyless POST sends `Content-Length: 0` correctly.

---

### 🔴 QA5 — Redirect Behavior Not Tested At All

The `HttpClient.Builder` is correctly configured with `.followRedirects(HttpClient.Redirect.NEVER)` so that HtmlUnit manages redirects. But:

1. No test verifies that 301/302/303/307 redirects are properly NOT followed by the JDK client.
2. No test verifies that HtmlUnit's redirect loop detection still works with the JDK engine.
3. No test verifies that POST-to-GET redirect semantics (303) are handled correctly.

**Comparison:** The parameterized integration tests cover static content and DELETE bodies, but the **redirect** path — one of the most common sources of behavioral divergence across HTTP engines — has zero coverage.

**Suggestion:** This needs at minimum a parameterized redirect test: set up a server that returns 302, verify the JDK engine returns the redirect response (not the final response), and that HtmlUnit follows it.

---

### 🟡 QA6 — Response Body Stream Not Read Before Timeout Could Expire

In `executeRequest()`, the `BodyHandler` wraps the response to track `read()` bytes. But the actual body reading happens later in `makeWebResponse()` via `responseBody.readAllBytes()`. Between the `httpClient.send()` return and `readAllBytes()`, the request's per-request timeout has already started counting.

For very slow-dripping responses (think: a 100MB file at 1 byte/second), the per-request timeout might expire *during* `readAllBytes()`, causing an `HttpTimeoutException` to be thrown inside `makeWebResponse()`. This is actually *correct* behavior, but it's not tested and the exception would propagate as an `IOException`, which `AbstractWebConnection.getResponse()` does handle. Worth verifying.

---

### 🟡 QA7 — `IS_JDK_THREAD` Never Cleaned Up on Thread Pool Shutdown

When `close()` calls `executor.shutdownNow()`, the pooled threads may be interrupted mid-work. But `IS_JDK_THREAD` won't be cleaned from those threads' ThreadLocal storage because the `finally` block in the ThreadFactory wrapper only runs `IS_JDK_THREAD.remove()` when the thread's `Runnable` completes normally or with an exception — not when `Thread.interrupt()` is called from `shutdownNow()`.

This could cause ThreadLocal leaks in environments where threads are pooled at a higher level (e.g., application server deployments). For a JVM that exits after a load test, this is harmless. But if XLT were embedded in a long-running process, the threading plumbing could accumulate garbage.

---

## Role 8: Grumpy OG Developer 😤

*"I wrote the original XLT HTTP stack back when Java 6 was cutting edge. I've seen every broken HTTP server, every truncated chunk-encoded response, every socket reset at 3 AM during Black Friday load tests. And now an AI wrote an HTTP client in one afternoon? Let me see this thing."*

---

### 😤 OG1 — Where Is the Class Javadoc?

[JdkWebConnection.java:77](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L77)

```java
public class JdkWebConnection extends AbstractWebConnection<HttpClient, HttpRequest, HttpResponse<InputStream>>
```

No class-level Javadoc. Our `AGENTS.md` says to comment everything extensively. The `OkHttp3WebConnection` has:
```java
/**
 * An alternative {@link WebConnection} implementation that uses OkHttp3 as HTTP client.
 */
```

This new class has... nothing. Not a single sentence explaining *what* this is, *why* it exists, what JDK version it requires, what its limitations are (connect time, heuristic metrics, no cookie jar...). When some poor developer opens this file in 2028, they'll have no context.

I've been doing this for 20 years. The code tells you *what*. The comments tell you *why*. An AI that doesn't explain its own code is an AI that creates maintenance debt.

---

### 😤 OG2 — `collectTargetIpAddress` Field Is Stored, `@SuppressWarnings("unused")`, and Never Used

[JdkWebConnection.java:104-105](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L104)

```java
@SuppressWarnings("unused")
private final boolean collectTargetIpAddress;
```

So the AI *knows* it's unused and annotated it to suppress the warning. That's not a solution — that's papering over a gap. In OkHttp3, this flag controls whether the `RetrieveUsedTargetIpAddressInterceptor` is installed. In the JDK engine: nothing.

This field is a promise to the caller that IP address collection can be enabled. The caller in `XltWebClient.configureWebConnection()` passes `collectTargetIpAddress` as `true` when configured. The JDK engine silently ignores it.

**My verdict:** Either implement IP address collection (use the DNS resolver's results) or throw `UnsupportedOperationException` if someone tries to enable it. Don't silently swallow feature flags. That's how you get bug reports that take days to diagnose: *"Why does the IP column in my report say nothing when I switched to the JDK engine?"*

---

### 😤 OG3 — This is 583 Lines and I See Exactly Zero Logging Statements

The entire `JdkWebConnection.java` has **not a single log statement**. Not `LOG.debug()`, not `LOG.warn()`, not even `LOG.error()`. There is no `private static final Logger` field.

Let me compare:
- **XltWebClient:** Uses `XltLogger.runTimeLogger` extensively for proxy config, SSL, browser version, timeouts.
- **OkHttp3WebConnection:** Admittedly also light on logging, but at least it's a thin wrapper around OkHttp which has its own HTTP logging interceptor.
- **JdkWebConnection:** A 583-line class that does SSL setup, proxy config, DNS resolution, multipart uploads, metric instrumentation, response parsing — and if anything goes wrong, there's **no diagnostic trace whatsoever**.

When a customer calls at 2 AM saying "the JDK engine is failing against their edge proxy," we'll turn on `DEBUG` logging and get *nothing*. We'll ask them to switch back to Apache.

At minimum: log when the HttpClient is created (what SSL mode, what proxy, what timeout). Log when it's closed. Log when DNS resolution falls through to the builtin.

---

### ~~😤 OG4 — The Methanol Dependency Concerns Me~~ ✅ DISMISSED

*Dismissed by reviewer — Methanol dependency is acceptable.*

---

### 😤 OG5 — This is a JVM-wide SPI Hook. Do You Understand What That Means?

[XltInetAddressResolverProvider.java](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/XltInetAddressResolverProvider.java)

This `META-INF/services` registration means that **every single `InetAddress.getByName()` call in the entire JVM** goes through our code. Every. Single. One.

That includes:
- HtmlUnit's internal resolution
- Apache HttpClient's resolution (for the default engine!)
- Any library doing DNS lookups (database drivers, cloud SDK clients, etc.)
- Java's own SSL hostname verification
- Even our `XltDnsResolver`'s own `PlatformHostNameResolver` calling `InetAddress.getAllByName()` (hence the reentrancy guard)

Yes, the `IS_JDK_THREAD` guard limits XLT-specific behavior to JDK HttpClient threads. But the **performance overhead** of checking two `ThreadLocal.get()` calls on every DNS resolution in the JVM is non-zero. In a load test running 500 virtual users each making 100 requests/second, that's 50,000+ ThreadLocal reads per second *just for the guard check.

Also: what happens when we deploy the XLT agent alongside other tooling in the same JVM? We've just silently taken over DNS for the whole process. That's an act of war.

I understand *why* this is needed (JDK HttpClient has no DNS hook), but this needs a big fat warning in the documentation. And ideally, the SPI should be loaded only when the JDK engine is actually selected — is that possible via a no-op default that activates lazily?

---

### 😤 OG6 — The BodySubscriber Wrapper is Fragile

[JdkWebConnection.java:462-502](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L462)

The hand-rolled `BodySubscriber<InputStream>` wrapper intercepts `onNext()` to track received bytes. But it's a *delegating* wrapper — every method just calls through to `baseSubscriber`. This is textbook fragile delegation:

1. If a future JDK version adds a new method to `BodySubscriber` (it's an interface — but `Flow.Subscriber` could evolve), our wrapper silently misses it.
2. If the `baseSubscriber` has internal state that depends on `onNext()` being called from specific threads, we might break it.
3. The `ByteBuffer.remaining()` call in `onNext()` reads the buffer position without consuming — this is correct for tracking, but if anyone ever changes the iteration order, it could double-count.

I've seen delegating wrappers go wrong so many times. They work until they don't, and then they fail in the most confusing way possible.

The approach is sound — I just want to see a comment acknowledging the fragility, and ideally a test that exercises the wrapper under chunked transfer encoding.

---

### ~~😤 OG7 — You Replaced `jdk26` with `jdk` but Left No Migration Trail~~ ✅ DISMISSED

*Dismissed by reviewer — no migration path needed, `jdk26` was never released.*

---

### 😤 OG8 — RuntimeException for Missing Upload Files? Really?

[JdkWebConnection.java:335-337](file:///home/rschwietzke/projects/GIT/XLT.xslt/src/main/java/com/xceptance/xlt/engine/htmlunit/jdk/JdkWebConnection.java#L335)

```java
} catch (Exception e) {
    throw new RuntimeException("Cannot find upload file: " + filePair.getFile(), e);
}
```

First, this catch block is pointless — the only thing inside the `try` is `BodyPublishers.ofInputStream()` and `builder.formPart()`, neither of which throw checked exceptions. The `ofInputStream()` creates a lazy publisher that defers the `Files.newInputStream()` call. So this catch will **never** fire for missing files — the `FileNotFoundException` will surface later during body publishing, wrapped in `UncheckedIOException`, and blow up somewhere deep inside `httpClient.send()`.

Second, even if it did fire, `RuntimeException` is the wrong type. Use `XltException` or `IOException` like the rest of the codebase.

This feels like code that was written to "handle errors" without actually testing whether those errors can occur here. Classic AI pattern: it *looks* correct at first glance.

---

### 😤 OG9 — I Don't Trust the Metric Numbers

The entire `executeRequest()` method is 120 lines of metric simulation. Let me count what's *actual* vs. *heuristic*:

| Metric | Real or Heuristic? |
|---|---|
| DNS Time | Real (via SPI hook) ✅ |
| Connect Time | Heuristic: always 0ms ❌ |
| Bytes Sent | Heuristic: URI + 200 + content length ❌ |
| TTFB | Partially real: `BodyHandler.apply()` timing ⚠️ |
| Bytes Received | Real: `onNext()` tracking ✅ |
| Response Header Bytes | Heuristic: 256 + header lengths ❌ |

So 3 out of 6 metrics are heuristic, and 1 is partially real. For a **performance testing tool**, half the socket-level metrics being fake is a serious concern. Users comparing JDK engine reports to Apache/OkHttp3 reports will get different numbers for the same test scenario. That's not a bug — that's a credibility problem.

I'm not saying this can't ship. But the documentation needs to be crystal clear: *"When using `com.xceptance.xlt.http.client=jdk`, connect time will always report as 0ms and byte counts are estimates. For precise network metrics, use the default Apache engine."*

---

## Summary Table


| ID | Severity | Category | Summary |
|---|---|---|---|
| P1 | 🔴 | Performance | `readAllBytes()` buffers entire response — potential OOM at scale |
| P2 | 🟡 | Performance | Unbounded thread pool — could spike under burst traffic |
| P3 | 🟡 | Performance | Heuristic byte counting underestimates real traffic |
| P4 | 🟡 | Performance | `XltDnsResolver` created per-resolution (expensive constructor) |
| P5 | 🟢 | Performance | Connect time always 0ms (documented, acceptable) |
| P6 | 🟢 | Performance | Static SSL context initialization (well done) |
| D2 | 🟡 | Design | HttpClient cached per transaction, proxy changes ignored |
| D3 | 🟡 | Design | `IS_JDK_THREAD` is `public` but package-private suffices |
| D5 | 🟢 | Design | Protocol version mapping is correct |
| D6 | 🟡 | Design | Apache `EnglishReasonPhraseCatalog` cross-dependency |
| S1 | 🟢 | Security | Insecure SSL guard is correct |
| S2 | 🟡 | Security | `AuthScope.ANY` fallback (pre-existing pattern) |
| C1 | 🔴 | Correctness | Dual `IS_JDK_THREAD` marking — clarify intent with comments |
| C2 | 🟡 | Concurrency | `createHttpClient()` not thread-safe |
| C4 | 🟡 | Correctness | Silent fallback swallows exceptions without logging |
| T1 | 🔴 | Testing | Protocol version assertion is wrong (`"HTTP_2"` vs `"h2"`) |
| T2 | 🟡 | Testing | Stale comment + wrong assertion for reason phrase |
| T3 | 🟡 | Testing | SSL rejection assertion too weak |
| T4 | 🟡 | Testing | Null-context test is a no-op |
| T6 | 🟡 | Testing | No timeout behavior test |
| T7 | 🟡 | Testing | No proxy configuration test |
| CS1 | 🟡 | Standards | Duplicate `ProxySelector` import |
| CS2 | 🟡 | Standards | Import order not standard |
| CS3 | 🟡 | Standards | Allman style violations |
| CS4 | 🟡 | Standards | Missing Java 21 pattern matching |
| CS5 | 🟡 | Standards | Missing Javadoc on inner class constructor |
| QA1 | 🔴 | QA | No cookie handling — risk of silent session bugs |
| QA2 | 🟡 | QA | Multipart missing edge case (file and data both null) |
| QA3 | 🟡 | QA | No builder-level connect timeout (Linux: 60–120s hang) |
| QA4 | 🟡 | QA | No test for empty POST body |
| QA5 | 🔴 | QA | Zero redirect behavior coverage |
| QA6 | 🟡 | QA | Timeout during slow-drip response reading untested |
| QA7 | 🟡 | QA | ThreadLocal leak on `shutdownNow()` |
| OG1 | 😤 | OG Dev | No class-level Javadoc |
| OG2 | 😤 | OG Dev | `collectTargetIpAddress` stored but never used |
| OG3 | 😤 | OG Dev | Zero logging statements in 583 lines |
| ~~OG4~~ | ~~😤~~ | ~~OG Dev~~ | ~~Methanol dependency~~ — DISMISSED |
| OG5 | 😤 | OG Dev | JVM-wide SPI hook is invasive — needs documentation |
| OG6 | 😤 | OG Dev | BodySubscriber delegation is fragile |
| ~~OG7~~ | ~~😤~~ | ~~OG Dev~~ | ~~`jdk26` → `jdk` rename~~ — DISMISSED |
| OG8 | 😤 | OG Dev | Dead catch block — can never fire |
| OG9 | 😤 | OG Dev | Half the metrics are heuristic — credibility risk |

---

## Verdict

The implementation is **architecturally sound** and integrates cleanly into the existing `AbstractWebConnection` framework. The DNS SPI hook is a clever solution for the JDK HttpClient's lack of a pluggable DNS resolver. The test coverage is broad, especially with the parameterized integration tests.

However, the QA and OG Developer reviews surfaced several findings that the earlier technical reviews missed — most notably the **absent cookie handling** (QA1), the **missing redirect tests** (QA5), the **zero logging** (OG3), and the **unused `collectTargetIpAddress` feature flag** (OG2). These are the kind of issues that don't show up in unit tests but explode in production under real load.

**Must-fix before merge:**
1. **T1/T2**: The test assertions for protocol version and reason phrase appear to be factually incorrect. Run the tests and verify.
2. **QA1**: Explicitly disable JDK cookie handling to prevent silent session conflicts.
3. **QA2**: Add the missing `else` branch for null-file null-data `KeyDataPair`.
4. **OG2**: Either implement `collectTargetIpAddress` or remove the field and throw/log if someone enables it.
5. **OG3**: Add at minimum creation/close/fallback logging.
6. **CS1**: Remove duplicate import.
7. **CS3**: Fix the 2-3 Allman style violations.

**Should-fix:**
1. **P2**: Bound the thread pool.
2. **P4**: Cache `XltDnsResolver` instance in the SPI hook.
3. **C4**: Add logging before silent fallback in DNS resolver.
4. **C1**: Add comments clarifying the dual `IS_JDK_THREAD` marking.
5. **QA3**: Add builder-level connect timeout to avoid OS-default TCP hangs.
6. **OG1**: Add class-level Javadoc documenting purpose, JDK requirements, and known limitations.
7. **OG8**: Remove dead catch block or restructure.

**Consider for a follow-up:**
1. **P1**: Investigate streaming response bodies instead of `readAllBytes()`.
2. **QA5**: Add redirect behavior tests — critical gap.
3. **T6/T7**: Add timeout and proxy tests.
4. **D6**: Consider removing Apache `EnglishReasonPhraseCatalog` dependency.
5. **OG5**: Document the JVM-wide SPI impact in user-facing documentation.
7. **OG9**: Document metric accuracy limitations clearly in the user guide.
