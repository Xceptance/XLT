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
package com.xceptance.xlt.mastercontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import util.JUnitParamsUtils;

@RunWith(JUnitParamsRunner.class)
public class XltProxySelectorTest
{
    private final String PROXY_HOST = " example.org ";

    private final String PROXY_PORT = " 9000 ";

    private final Proxy TEST_PROXY = new Proxy(Proxy.Type.HTTP,
                                               new InetSocketAddress(PROXY_HOST.trim(), Integer.valueOf(PROXY_PORT.trim())));

    @Test
    public void select() throws URISyntaxException
    {
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, null, null);

        // Proxy is used for all HTTPS hosts
        validateProxy(TEST_PROXY, selector.select(new URI("https://example.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://foo.bar.test")));
    }

    @Test
    public void select_singleInclude() throws URISyntaxException
    {
        // hostname includes contain one regex pattern
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, "fo.*ar", null);

        // Use proxy for hosts that match the include pattern
        validateProxy(TEST_PROXY, selector.select(new URI("https://foo.bar.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://foar.test")));

        // Bypass proxy for all other hosts
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://any.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://bar.foo.test")));
    }

    @Test
    public void select_multipleIncludes() throws URISyntaxException
    {
        // hostname includes contain multiple regex patterns
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, "example fo.*ar", null);

        // Use proxy for hosts that match the include patterns
        validateProxy(TEST_PROXY, selector.select(new URI("https://example.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://foo.bar.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://foo.example.bar.test")));

        // Bypass proxy for all other hosts
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://any.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://bar.foo.test")));
    }

    @Test
    public void select_singleExclude() throws URISyntaxException
    {
        // hostname excludes contain one regex pattern
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, null, "fo.*ar");

        // Bypass proxy for all hosts that match the exclude pattern
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://foo.bar.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://foar.test")));

        // Use proxy for all other hosts
        validateProxy(TEST_PROXY, selector.select(new URI("https://any.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://bar.foo.test")));
    }

    @Test
    public void select_multipleExcludes() throws URISyntaxException
    {
        // hostname excludes contain multiple regex patterns
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, null, "example fo.*ar");

        // Bypass proxy for all hosts that match the exclude patterns
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://example.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://foo.bar.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://foo.example.bar.test")));

        // Use proxy for all other hosts
        validateProxy(TEST_PROXY, selector.select(new URI("https://any.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://bar.foo.test")));
    }

    @Test
    public void select_includesAndExcludes() throws URISyntaxException
    {
        // host name includes and excludes contain multiple regex patterns
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, "example fo.*ar", "bypass ignore");

        // Host matches neither include nor exclude patterns; bypass proxy
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://any.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://bar.foo.test")));

        // Host matches only include patterns; use proxy
        validateProxy(TEST_PROXY, selector.select(new URI("https://example.test")));
        validateProxy(TEST_PROXY, selector.select(new URI("https://foo.example.bar.test")));

        // Host matches only exclude patterns; bypass proxy
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://ignore.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://bypass.ignore.test")));

        // Host matches include and exclude patterns; bypass proxy
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://bypass.example.test")));
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("https://foo.bypass.example.ignore.bar.test")));
    }

    @Test
    public void select_nonHttpsHost() throws URISyntaxException
    {
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, null, null);

        // host protocol isn't "https", so don't use the proxy
        validateProxy(Proxy.NO_PROXY, selector.select(new URI("http://example.test")));
    }

    @Test
    public void select_error_uriIsNull()
    {
        // host URI must not be null
        final XltProxySelector selector = new XltProxySelector(PROXY_HOST, PROXY_PORT, null, null);
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> selector.select(null));
        assertEquals("URI can't be null.", ex.getMessage());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void error_proxyHostIsBlankOrNull(final String blankStringOrNull)
    {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> new XltProxySelector(blankStringOrNull, PROXY_PORT, null, null));
        assertEquals("Proxy host must not be NULL or empty.", ex.getMessage());
    }

    @Test
    public void error_proxyPortIsNotANumber()
    {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> new XltProxySelector(PROXY_HOST, "abc", null, null));
        assertEquals("Proxy port must be a number, but was: abc", ex.getMessage());
    }

    @Test
    public void error_invalidHostIncludePattern()
    {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> new XltProxySelector(PROXY_HOST, PROXY_PORT, "example ([]-]", null));
        assertEquals("Proxy hostname include string contains invalid regex patterns: example ([]-]", ex.getMessage());
    }

    @Test
    public void error_invalidHostExcludePattern()
    {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                         () -> new XltProxySelector(PROXY_HOST, PROXY_PORT, null, "example ([]-]"));
        assertEquals("Proxy hostname exclude string contains invalid regex patterns: example ([]-]", ex.getMessage());
    }

    /**
     * Helper method to validate that a given list of proxies contains only one expected proxy.
     *
     * @param expected
     *            the expected proxy
     * @param proxies
     *            the list of proxies
     */
    private void validateProxy(final Proxy expected, final List<Proxy> proxies)
    {
        assertEquals(1, proxies.size());
        assertEquals(expected, proxies.get(0));
    }
}
