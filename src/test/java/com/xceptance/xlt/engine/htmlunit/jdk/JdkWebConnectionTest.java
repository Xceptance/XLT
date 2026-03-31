/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine.htmlunit.jdk;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractServerTestCase;

public class JdkWebConnectionTest extends AbstractServerTestCase
{
    private WebClient webClient;
    private TestableJdkWebConnection connection;

    @Before
    public void setup()
    {
        webClient = new WebClient();
        connection = new TestableJdkWebConnection(webClient);
    }
    
    @After
    public void teardown()
    {
        if (connection != null)
        {
            connection.close();
        }
        if (webClient != null)
        {
            webClient.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testHeaderFiltering() throws Exception
    {
        WebRequest webRequest = new WebRequest(new URL("https://example.com"), HttpMethod.GET);
        webRequest.setAdditionalHeader("Host", "fake-host");
        webRequest.setAdditionalHeader("Connection", "keep-alive");
        webRequest.setAdditionalHeader("Content-Length", "500");
        webRequest.setAdditionalHeader("X-Custom-Header", "ValidValue");

        HttpRequest request = connection.createRequestTesting(URI.create("https://example.com"), webRequest, HttpRequest.BodyPublishers.noBody());
        
        // Assert filtered headers were removed (JDK HttpClient manages structural headers itself)
        Assert.assertFalse(request.headers().map().containsKey("Host"));
        Assert.assertFalse(request.headers().map().containsKey("Connection"));
        Assert.assertFalse(request.headers().map().containsKey("Content-Length"));
        
        // Assert custom headers remained
        Assert.assertEquals("ValidValue", request.headers().firstValue("X-Custom-Header").get());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testMakeWebResponseMapping() throws Exception
    {
        WebRequest webRequest = new WebRequest(new URL("https://example.com"), HttpMethod.GET);
        
        HttpResponse<java.io.InputStream> mockResponse = new MockHttpResponse(200, "Mock Payload", HttpClient.Version.HTTP_2);
        
        WebResponse webResponse = connection.makeWebResponseTesting(mockResponse, webRequest, 150L);
        
        Assert.assertEquals(200, webResponse.getStatusCode());
        Assert.assertEquals("HTTP_2", webResponse.getProtocolVersion());
        // Since reason phrase mapping is not yet implemented (Issue #6), it will be empty
        Assert.assertEquals("", webResponse.getStatusMessage());
        Assert.assertEquals(150L, webResponse.getLoadTime());
        Assert.assertEquals("Mock Payload", webResponse.getContentAsString());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testCloseCleansUpResources() throws Exception
    {
        // Force initialization
        connection.forceInitialization();
        
        Assert.assertNotNull(connection.getHttpClient());
        Assert.assertNotNull(connection.getExecutor());
        
        connection.close();
        
        Assert.assertNull(connection.getHttpClient());
        Assert.assertNull(connection.getExecutor());
    }

    // Helper class to expose protected methods of JdkWebConnection
    private static class TestableJdkWebConnection extends JdkWebConnection
    {
        public TestableJdkWebConnection(WebClient webClient)
        {
            super(webClient, false);
        }

        public HttpRequest createRequestTesting(URI uri, WebRequest webRequest, HttpRequest.BodyPublisher bodyPublisher) throws Exception
        {
            // Use reflection since createRequest is private
            java.lang.reflect.Method m = JdkWebConnection.class.getDeclaredMethod("createRequest", URI.class, WebRequest.class, HttpRequest.BodyPublisher.class);
            m.setAccessible(true);
            return (HttpRequest) m.invoke(this, uri, webRequest, bodyPublisher);
        }

        public WebResponse makeWebResponseTesting(HttpResponse<java.io.InputStream> response, WebRequest webRequest, long loadTime) throws IOException
        {
            return makeWebResponse(response, webRequest, loadTime);
        }
        
        public void forceInitialization() throws Exception {
            java.lang.reflect.Method m = JdkWebConnection.class.getDeclaredMethod("createHttpClient", WebClient.class, WebRequest.class);
            m.setAccessible(true);
            m.invoke(this, this.getWebClient(), new WebRequest(new URL("https://example.com"), HttpMethod.GET));
        }
        
        public HttpClient getHttpClient() {
            try {
                java.lang.reflect.Field f = JdkWebConnection.class.getDeclaredField("httpClient");
                f.setAccessible(true);
                return (HttpClient) f.get(this);
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        public java.util.concurrent.ExecutorService getExecutor() {
            try {
                java.lang.reflect.Field f = JdkWebConnection.class.getDeclaredField("executor");
                f.setAccessible(true);
                return (java.util.concurrent.ExecutorService) f.get(this);
            } catch (Exception e) { throw new RuntimeException(e); }
        }
    }

    // Simple Mock for HttpResponse
    private static class MockHttpResponse implements HttpResponse<java.io.InputStream>
    {
        private final int statusCode;
        private final String payload;
        private final HttpClient.Version version;

        public MockHttpResponse(int statusCode, String payload, HttpClient.Version version)
        {
            this.statusCode = statusCode;
            this.payload = payload;
            this.version = version;
        }

        @Override
        public int statusCode() { return statusCode; }

        @Override
        public HttpRequest request() { return null; }

        @Override
        public Optional<HttpResponse<java.io.InputStream>> previousResponse() { return Optional.empty(); }

        @Override
        public HttpHeaders headers() { return HttpHeaders.of(Map.of("Content-Type", List.of("text/plain")), (h1, h2) -> true); }

        @Override
        public java.io.InputStream body() { return new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)); }

        @Override
        public Optional<SSLSession> sslSession() { return Optional.empty(); }

        @Override
        public URI uri() { return URI.create("https://example.com"); }

        @Override
        public HttpClient.Version version() { return version; }
    }
}
