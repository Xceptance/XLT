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
package com.xceptance.xlt.api.engine;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * @author Sebastian Oerding
 */
public class RequestFilterTest
{
    private static final String HEISE = "http://www.heise.de";

    @Test
    public void testAcceptsDefault() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE));

        Assert.assertEquals("Default return value changed!", false, filter.accepts(null));
        Assert.assertEquals("Default return value changed! Request is no longer accepted!", true, filter.accepts(request));
    }

    @Test
    public void testAcceptByUrlPattern() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE));
        filter.setUrlPattern(".*blo.*");
        Assert.assertEquals("Return value when checking with Url pattern changed! Request is accepted!", false, filter.accepts(request));
        filter.setUrlPattern(".*heise.*");
        Assert.assertEquals("Return value when checking with Url pattern changed! Request is no longer accepted!", true,
                            filter.accepts(request));
    }

    @Test
    public void testAcceptByProtocol() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE + "?bla"));
        filter.setProtocol("ftp");
        Assert.assertEquals("Return value when checking protocol changed! Request is accepted!", false, filter.accepts(request));
        filter.setProtocol("http");
        Assert.assertEquals("Return value when checking protocol changed! Request is no longer accepted!", true, filter.accepts(request));
    }

    @Test
    public void testAcceptByHostPattern() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE + "?bla"));
        filter.setHostPattern("schneise.de");
        Assert.assertEquals("Return value when checking with host pattern changed! Request is accepted!", false, filter.accepts(request));
        filter.setHostPattern("www.heise.de");
        Assert.assertEquals("Return value when checking with host pattern changed! Request is no longer accepted!", true,
                            filter.accepts(request));
    }

    @Test
    public void testAcceptByPathPattern() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE + "/newsticker"));
        filter.setPathPattern("blo");
        Assert.assertEquals("Return value when checking with path pattern changed! Request is accepted!", false, filter.accepts(request));
        filter.setPathPattern("/newsticker");
        Assert.assertEquals("Return value when checking with path pattern changed! Request is no longer accepted!", true,
                            filter.accepts(request));
    }

    @Test
    public void testAcceptByQueryPattern() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        final WebRequest request = new WebRequest(new URL(HEISE + "?bla"));
        filter.setQueryPattern(".*blo.*");
        Assert.assertEquals("Return value when checking with query pattern changed! Request is accepted!", false, filter.accepts(request));
        filter.setQueryPattern(".*bla.*");
        Assert.assertEquals("Return value when checking with query pattern changed! Request is no longer accepted!", true,
                            filter.accepts(request));
    }

    @Test
    public void testSimpleCalls()
    {
        final RequestFilter filter = new RequestFilter();
        filter.setProtocol("noProtocol");
        Assert.assertEquals("Wrong protocol, ", "noProtocol", filter.getProtocol());
        filter.setHostPattern(".*");
        Assert.assertEquals("Wrong host pattern, ", ".*", filter.getHostPattern());
        filter.setPort(1);
        Assert.assertEquals("Wrong port, ", 1, filter.getPort());
        filter.setPathPattern("\\d+");
        Assert.assertEquals("Wrong path pattern, ", "\\d+", filter.getPathPattern());
        filter.setQueryPattern("query");
        Assert.assertEquals("Wrong host pattern, ", "query", filter.getQueryPattern());
        filter.setUrlPattern("anUrl");
        Assert.assertEquals("Wrong host pattern, ", "anUrl", filter.getUrlPattern());
    }

    @Test
    public void testMismatchingPort() throws MalformedURLException
    {
        final RequestFilter filter = new RequestFilter();
        filter.setPort(1);
        final WebRequest request = new WebRequest(new URL("http://www.heise.de:80"));
        Assert.assertEquals("Return value when checking with port pattern changed! Request is accepted!", false, filter.accepts(request));
    }
}
