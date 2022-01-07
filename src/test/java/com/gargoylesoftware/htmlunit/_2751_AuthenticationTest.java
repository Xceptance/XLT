/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
/*
 * File: _2571_AuthenticationTest.java
 * Created on: Aug 3, 2016
 */
package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the fix for issue 2751.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2751_AuthenticationTest
{
    private static Server Server_NoAuth;

    private static Server Server_Auth;

    protected static Server configureServer(final int port, final String resourceBase, final boolean auth)
    {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(resourceBase);

        if (auth)
        {
            final Constraint constraint = new Constraint();
            constraint.setName(Constraint.__BASIC_AUTH);
            constraint.setRoles(new String[]
                {
                    "user"
                });
            constraint.setAuthenticate(true);

            final ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/*");

            final ConstraintSecurityHandler handler = (ConstraintSecurityHandler) context.getSecurityHandler();
            handler.setLoginService(new HashLoginService("MyRealm", "./src/test-hu/resources/realm.properties"));
            handler.setConstraintMappings(new ConstraintMapping[]
                {
                    constraintMapping
                });
            handler.setAuthenticator(new BasicAuthenticator());
        }

        WebAppContextWrapper contextWrapper = new WebAppContextWrapper(context);

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(contextWrapper);

        final Server server = new Server(port);
        server.setHandler(handlers);

        return server;
    }

    @BeforeClass
    public static void startServers() throws Exception
    {
        Server_Auth = configureServer(9080, "./", true);
        Server_Auth.start();

        Server_NoAuth = configureServer(9081, "./", false);
        Server_NoAuth.start();
    }

    @AfterClass
    public static void stopServers() throws Exception
    {
        if (Server_Auth != null)
            Server_Auth.stop();
        if (Server_NoAuth != null)
            Server_NoAuth.stop();
    }

    @Test
    public void testAuth_HostScoped() throws Exception
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getCache().setMaxSize(0);
            wc.getCredentialsProvider().setCredentials(new AuthScope("localhost", 9080, "MyRealm"),
                                                       new UsernamePasswordCredentials("jetty", "jetty"));

            // no authentication required
            WebAppContextWrapper noAuthContextWrapper = (WebAppContextWrapper) ((HandlerList) Server_NoAuth.getHandlers()[0]).getHandlers()[0];

            Assert.assertEquals(200, wc.getPage("http://localhost:9081/build.xml").getWebResponse().getStatusCode());
            Assert.assertEquals("Should not got a 401", 0, noAuthContextWrapper.getNumberOfUnauthorizedResponses());
            Assert.assertEquals("Should not send an authorization header", 0, noAuthContextWrapper.getNumberOfAuthRequestHeaders());
            Assert.assertEquals("Unexpected number of requests", 1, noAuthContextWrapper.getNumberOfRequests());

            // authentication required
            WebAppContextWrapper authContextWrapper = (WebAppContextWrapper) ((HandlerList) Server_Auth.getHandlers()[0]).getHandlers()[0];

            Assert.assertEquals(200, wc.getPage("http://localhost:9080/build.xml").getWebResponse().getStatusCode());
            Assert.assertEquals("Should got a 401", 1, authContextWrapper.getNumberOfUnauthorizedResponses());
            Assert.assertEquals("Should send authorization header", 1, authContextWrapper.getNumberOfAuthRequestHeaders());
            Assert.assertEquals("Unexpected number of requests", 2, authContextWrapper.getNumberOfRequests());
        }
    }

    /**
     * Wrapper around a {@link WebAppContext} object that allows inspection of certain details.
     */
    private static class WebAppContextWrapper extends HandlerWrapper
    {
        private WebAppContext context;

        private int authRequestHeaders;

        private int unauthorizedResponses;

        private int requests;

        public WebAppContextWrapper(WebAppContext context)
        {
            this.context = context;

            setHandler(context);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
        {
            context.handle(target, baseRequest, request, response);

            requests++;

            if (request.getHeader(HttpHeader.AUTHORIZATION.asString()) != null)
            {
                authRequestHeaders++;
            }

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401)
            {
                unauthorizedResponses++;
            }
        }

        public int getNumberOfRequests()
        {
            return requests;
        }

        public int getNumberOfAuthRequestHeaders()
        {
            return authRequestHeaders;
        }

        public int getNumberOfUnauthorizedResponses()
        {
            return unauthorizedResponses;
        }
    }
}
