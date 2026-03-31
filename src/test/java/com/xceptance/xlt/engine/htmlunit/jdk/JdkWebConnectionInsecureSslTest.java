package com.xceptance.xlt.engine.htmlunit.jdk;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.htmlunit.WebClient;
import org.htmlunit.WebResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractServerTestCase;

public class JdkWebConnectionInsecureSslTest extends AbstractServerTestCase
{
    private JdkWebConnection connection;
    private Server server;
    private String baseUrl;

    @Before
    public void setUp() throws Exception
    {
        server = new Server();

        // 1. SSL context
        URL keystoreUrl = JdkWebConnectionInsecureSslTest.class.getClassLoader().getResource("self-signed-cert.keystore");
        if (keystoreUrl == null) {
            // fallback, sometimes test-hu resources aren't on classpath of xlt tests
            java.io.File fallback = new java.io.File("src/test-hu/resources/self-signed-cert.keystore");
            if (fallback.exists()) keystoreUrl = fallback.toURI().toURL();
        }
        
        SslContextFactory.Server contextFactory = new SslContextFactory.Server();
        contextFactory.setKeyStorePath(keystoreUrl.toExternalForm());
        contextFactory.setKeyStorePassword("nopassword");
        contextFactory.setKeyStoreType("jks");

        // 2. Connector
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new org.eclipse.jetty.server.SecureRequestCustomizer());
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(contextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(0);
        server.addConnector(sslConnector);

        // 3. Handler
        server.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException
            {
                response.setStatus(200);
                response.getWriter().print("SecureHello");
                baseRequest.setHandled(true);
            }
        });

        server.start();
        baseUrl = "https://localhost:" + sslConnector.getLocalPort() + "/";
    }

    @After
    public void tearDown() throws Exception
    {
        if (server != null)
        {
            server.stop();
        }
    }

    @Test
    public void testInsecureSslAllowed() throws Exception
    {
        try (WebClient webClient = new WebClient())
        {
            webClient.setWebConnection(new JdkWebConnection(webClient, false));
            webClient.getOptions().setUseInsecureSSL(true);

            WebResponse response = webClient.getPage(baseUrl).getWebResponse();
            Assert.assertEquals(200, response.getStatusCode());
            Assert.assertEquals("SecureHello", response.getContentAsString());
        }
    }
    
    @Test
    public void testInsecureSslRejected() throws Exception
    {
        try (WebClient webClient = new WebClient())
        {
            webClient.setWebConnection(new JdkWebConnection(webClient, false));
            webClient.getOptions().setUseInsecureSSL(false); // default

            try {
                webClient.getPage(baseUrl);
                Assert.fail("Should have thrown SSLHandshakeException");
            } catch (Exception e) {
                // Should fail due to untrusted cert. Might be wrapped depending on internal logic.
                Assert.assertTrue(e.getMessage() != null || e.getCause() != null);
            }
        }
    }
}
