# DNS Challenges in java.net.http.HttpClient

Unlike OkHttp (which provides a clean `Dns` interface to intercept hostname-to-IP resolution on a per-client basis) or Apache HttpClient (which provides a `DnsResolver`), the JDK `java.net.http.HttpClient` does not expose a direct networking SPI for DNS resolution on the builder instance. In XLT, overriding DNS (via `.hosts` or dynamic test mapping) is a core feature that we must support.

Here are the concrete alternatives for bridging XLT's `DnsImpl` into the JDK 26 `HttpClient`:

## Alternative 1: Custom JVM `InetAddressResolverProvider` (JEP 418 - Recommended)

Introduced in JDK 18, the JDK finally provided a standard Service Provider Interface (SPI) for name resolution. 

- **How it works:** We implement `java.net.spi.InetAddressResolverProvider` and register it. All standard `InetAddress.getByName()` calls in the JVM will route through our provider. Inside our implementation, we hook directly into `DnsImpl`.
- **Pros:**
  - 100% native integration. The HttpClient uses the standard `InetAddress` hooks under the hood, so it will seamlessly honor our overrides.
  - Fixes SNI completely: Since the HttpClient still thinks it's connecting to `example.com` (but gets `1.2.3.4` from our resolver), the TLS/SNI handshakes succeed automatically.
- **Cons:**
  - Global SPI registration: It alters name resolution for the *entire JVM*. We must ensure our custom resolver smoothly delegates to the OS default resolver for non-XLT traffic, and that it honors XLT's thread-local transaction context correctly.

## Alternative 2: Custom `ProxySelector` Hacks

This involves configuring the `HttpClient` builder with a custom `ProxySelector`.

- **How it works:** When the client asks the proxy selector for routes to `example.com`, our selector intercepts it and returns a `Proxy.Type.HTTP` or `SOCKS` pointing directly to the resolved IP address (`1.2.3.4`).
- **Pros:**
  - Configured per-HttpClient instance (no global JVM state).
- **Cons:**
  - Hacky and brittle. HTTP proxies wrap traffic differently than direct IP routing. It can break HTTP/3 QUIC negotiation or TLS SNI boundaries unless perfectly aligned.
  - Managing actual real web proxies alongside this fake "DNS proxy" becomes a complex nested logic nightmare.

## Alternative 3: URL Rewriting + Custom Header Injection

We intercept the request before handing it to the `HttpClient`.

- **How it works:** We change the URI from `https://example.com/api` to `https://1.2.3.4/api` using the IP resolved from `DnsImpl`. We then explicitly inject the header `Host: example.com`.
- **Pros:**
  - Localized exclusively to the URL building phase. No networking hacks required.
- **Cons:**
  - **Breaks TLS/SNI.** Modern CDNs and web servers (Cloudflare, AWS) will reject the TLS handshake because the client will attempt to negotiate TLS with a blank SNI or an SNI of `1.2.3.4`, which will mismatch the actual site certificate for `example.com`.

---

> [!IMPORTANT]
> **Recommendation:** 
> I highly recommend **Alternative 1 (JEP 418 InetAddressResolverProvider)**. Since XLT already runs in a controlled JVM environment and we are targeting JDK 26, tapping into the official `InetAddressResolver` SPI is the cleanest, most robust way to guarantee SNI and modern HTTP/3 protocols function correctly without deeply hacking proxy layers.

Which alternative should we lock into the `design.md` before generating the final tasks?
