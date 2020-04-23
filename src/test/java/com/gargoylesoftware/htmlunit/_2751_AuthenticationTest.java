/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Tests the fix for issue 2751.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2751_AuthenticationTest
{
    private static final Has HAS_401 = new Has("<< HTTP/1.1 401");

    private static final Has HAS_AUTH = new Has(">> Authorization");

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
        }

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(context);

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

    @Test(expected = FailingHttpStatusCodeException.class)
    public void testServerSetup() throws Exception
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            Assert.assertEquals(200, wc.getPage("http://localhost:9081/build.xml").getWebResponse().getStatusCode());
            wc.getPage("http://localhost:9080/build.xml");
        }
    }

    @Test
    public void testAuth_HostScoped() throws Exception
    {
        final Logger logger = Logger.getLogger("org.apache.http.headers");
        final Level oldLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);

        final MessageCollectingAppender appender = new MessageCollectingAppender();
        logger.addAppender(appender);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getCache().setMaxSize(0);
            wc.getCredentialsProvider().setCredentials(new AuthScope("localhost", 9080, "MyRealm"),
                                                       new UsernamePasswordCredentials("jetty", "jetty"));

            Assert.assertEquals(200, wc.getPage("http://localhost:9081/build.xml").getWebResponse().getStatusCode());

            final int nbNotAuthorized = appender.getMessageCount(HAS_401);
            Assert.assertEquals("Should not got a 401", 0, nbNotAuthorized);

            Assert.assertEquals(200, wc.getPage("http://localhost:9080/build.xml").getWebResponse().getStatusCode());
            Assert.assertEquals(nbNotAuthorized + 1, appender.getMessageCount(HAS_401));

            appender.clear();

            Assert.assertEquals(200, wc.getPage("http://localhost:9081/build.xml").getWebResponse().getStatusCode());
            Assert.assertEquals("Should not send an authorization header", 0, appender.getMessageCount(HAS_AUTH));

            Assert.assertEquals(200, wc.getPage("http://localhost:9080/build.xml").getWebResponse().getStatusCode());
            Assert.assertEquals("Should send authorization header", 1, appender.getMessageCount(HAS_AUTH));

        }
        finally
        {
            logger.removeAppender(appender);
            logger.setLevel(oldLevel);
        }
    }

    private static class MessageCollectingAppender implements Appender
    {
        private final List<String> messages_ = new ArrayList<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public void addFilter(Filter newFilter)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Filter getFilter()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clearFilters()
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close()
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doAppend(LoggingEvent event)
        {
            messages_.add(event.getRenderedMessage());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName()
        {
            return "RuntimeAppender-" + hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setErrorHandler(ErrorHandler errorHandler)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ErrorHandler getErrorHandler()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLayout(Layout layout)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Layout getLayout()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setName(String name)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean requiresLayout()
        {
            return false;
        }

        public int getMessageCount(final Predicate<String> predicate)
        {
            if (predicate == null)
            {
                return messages_.size();
            }

            return Collections2.filter(messages_, predicate).size();
        }

        public void clear()
        {
            messages_.clear();
        }
    }

    private static class Has implements Predicate<String>
    {
        private final String needle_;

        /**
         * 
         */
        public Has(final String needle)
        {
            this.needle_ = needle;
        }

        public boolean apply(final String input)
        {
            return input.contains(needle_);
        }
    }
}
