/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.util;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;

/**
 * Tests the implementation of utility class {@link UrlUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class UrlUtilsTest
{

    @Test
    public void testParseUrl_OnlyProto()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_ProtoHost()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://foo.bar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("foo.bar", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_ProtoHost_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://192.0.2.100");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_ProtoHost_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_UserInfoHost()
    {
        final URLInfo info = UrlUtils.parseUrlString("john:doe@foo.bar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertEquals("john:doe", info.getUserInfo());
        Assert.assertEquals("foo.bar", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_UserInfoHost_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("john:doe@192.0.2.100");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertEquals("john:doe", info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_UserInfoHost_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("john:doe@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertEquals("john:doe", info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_UserInfoOnly()
    {
        final URLInfo info = UrlUtils.parseUrlString("john:doe@");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertEquals("john:doe", info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostOnly()
    {
        final URLInfo info = UrlUtils.parseUrlString("example.org");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("example.org", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostOnly_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("192.0.2.100");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostOnly_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostPort()
    {
        final URLInfo info = UrlUtils.parseUrlString("example.org:999");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("example.org", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostPort_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("192.0.2.100:999");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_HostPort_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:999");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_PathOnly()
    {
        final URLInfo info = UrlUtils.parseUrlString("/foo/bar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("/foo/bar", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_PathQuery()
    {
        final URLInfo info = UrlUtils.parseUrlString("/foo/bar?baz=foo");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("/foo/bar", info.getPath());
        Assert.assertEquals("baz=foo", info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    public void testParseUrl_FragmentOnly()
    {
        final URLInfo info = UrlUtils.parseUrlString("#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_HostFragment()
    {
        final URLInfo info = UrlUtils.parseUrlString("example.org#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("example.org", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_HostFragment_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("192.0.2.100#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_HostFragment_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_QueryFragment()
    {
        final URLInfo info = UrlUtils.parseUrlString("?foo=bar#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_PathFragment()
    {
        final URLInfo info = UrlUtils.parseUrlString("/myPath/baz#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals("", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("/myPath/baz", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_CompleteUrl()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://john.doe:passwd@example.org:999/myPath/1/2?foo=bar#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals("example.org", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_CompleteUrl_IPv4()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://john.doe:passwd@192.0.2.100:999/myPath/1/2?foo=bar#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals("192.0.2.100", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_CompleteUrl_IPv6()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://john.doe:passwd@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:999/myPath/1/2?foo=bar#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    public void testParseUrl_CompleteUrl_IPv6_ShortFormat()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://john.doe:passwd@[2001:db8::1:0]:999/myPath/1/2?foo=bar#fooBar");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals("[2001:db8::1:0]", info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test(expected = NumberFormatException.class)
    public void testParseUrl_PortCannotBeParsed_PortIsNotAnInteger()
    {
        UrlUtils.parseUrlString("example.org:abc999");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseUrl_PortCannotBeParsed_AdditionalPortDelimiter()
    {
        UrlUtils.parseUrlString("example.org:9:99");
    }

    @Test
    public void testRewriteUrl_PathOnly() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("/myPath/1/2", URLInfo.builder().proto("http").host("example.org").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("example.org", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_PathOnly_IPv4() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("/myPath/1/2", URLInfo.builder().proto("http").host("192.0.2.100").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("192.0.2.100", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_PathOnly_IPv6() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("/myPath/1/2",
                                            URLInfo.builder().proto("http").host("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPath() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("example.org/myPath/1/2", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("example.org", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPath_IPv4() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("192.0.2.100/myPath/1/2", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("192.0.2.100", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPath_IPv6() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]/myPath/1/2", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPortPath() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("example.org:999/myPath/1/2", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("example.org", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(999, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPortPath_IPv4() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("192.0.2.100:999/myPath/1/2", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("192.0.2.100", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(999, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostPortPath_IPv6() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:999/myPath/1/2",
                                            URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(999, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostOnly() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("example.org", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("example.org", url.getHost());
        Assert.assertEquals("", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostOnly_IPv4() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("192.0.2.100", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("192.0.2.100", url.getHost());
        Assert.assertEquals("", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_HostOnly_IPv6() throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", url.getHost());
        Assert.assertEquals("", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    public void testRewriteUrl_RewriteCompleteUrl() throws Throwable
    {
        final URLInfo urlOverride = URLInfo.builder().proto("https").userInfo("admin:pw").host("foo.bar").port(1111).path("/myPath/baz")
                                           .query("abc=xyz").fragment("abcXyz").build();

        final URL info = UrlUtils.rewriteUrl("http://john.doe:passwd@example.org:999/myPath/1/2?foo=bar#fooBar", urlOverride);
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("https", info.getProtocol());
        Assert.assertEquals("admin:pw", info.getUserInfo());
        Assert.assertEquals("foo.bar", info.getHost());
        Assert.assertEquals("/myPath/baz", info.getPath());
        Assert.assertEquals("abc=xyz", info.getQuery());
        Assert.assertEquals("abcXyz", info.getRef());
        Assert.assertEquals(1111, info.getPort());
    }

    @Test
    public void testRewriteUrl_RewriteCompleteUrl_IPv4() throws Throwable
    {
        final URLInfo urlOverride = URLInfo.builder().proto("https").userInfo("admin:pw").host("203.0.113.200").port(1111)
                                           .path("/myPath/baz").query("abc=xyz").fragment("abcXyz").build();

        final URL info = UrlUtils.rewriteUrl("http://john.doe:passwd@192.0.2.100:999/myPath/1/2?foo=bar#fooBar", urlOverride);
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("https", info.getProtocol());
        Assert.assertEquals("admin:pw", info.getUserInfo());
        Assert.assertEquals("203.0.113.200", info.getHost());
        Assert.assertEquals("/myPath/baz", info.getPath());
        Assert.assertEquals("abc=xyz", info.getQuery());
        Assert.assertEquals("abcXyz", info.getRef());
        Assert.assertEquals(1111, info.getPort());
    }

    @Test
    public void testRewriteUrl_RewriteCompleteUrl_IPv6() throws Throwable
    {
        final URLInfo urlOverride = URLInfo.builder().proto("https").userInfo("admin:pw").host("[2001:db8::1:0]").port(1111)
                                           .path("/myPath/baz").query("abc=xyz").fragment("abcXyz").build();

        final URL info = UrlUtils.rewriteUrl("http://john.doe:passwd@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:999/myPath/1/2?foo=bar#fooBar",
                                             urlOverride);
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("https", info.getProtocol());
        Assert.assertEquals("admin:pw", info.getUserInfo());
        Assert.assertEquals("[2001:db8::1:0]", info.getHost());
        Assert.assertEquals("/myPath/baz", info.getPath());
        Assert.assertEquals("abc=xyz", info.getQuery());
        Assert.assertEquals("abcXyz", info.getRef());
        Assert.assertEquals(1111, info.getPort());
    }

    @Test
    public void testRewriteUrl_EmptyOverride() throws Throwable
    {
        // override is empty, so the original URL should remain unchanged
        final URL info = UrlUtils.rewriteUrl("http://john.doe:passwd@example.org:999/myPath/1/2?foo=bar#fooBar", URLInfo.builder().build());
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals("example.org", info.getHost());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getRef());
        Assert.assertEquals(999, info.getPort());
    }

    @Test
    public void testUrlEncodedParametersFromNameValuePairs()
    {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new NameValuePair("", "noKey"));
        parameters.add(new NameValuePair("noValue", ""));
        parameters.add(new NameValuePair("nullValue", null));
        parameters.add(new NameValuePair("aKey", "aValue"));

        String result = UrlUtils.getUrlEncodedParameters(parameters);

        // Validate
        List<org.apache.http.NameValuePair> parsedParams = URLEncodedUtils.parse(result, Charset.forName(XltConstants.UTF8_ENCODING));
        Assert.assertEquals("Unexpected number of parameters", 3, parsedParams.size());

        for (org.apache.http.NameValuePair eachParam : parsedParams)
        {
            switch (eachParam.getName())
            {
                case "noValue":
                    Assert.assertEquals("", eachParam.getValue());
                    break;
                case "nullValue":
                    Assert.assertEquals(null, eachParam.getValue());
                    break;
                case "aKey":
                    Assert.assertEquals("aValue", eachParam.getValue());
                    break;
                default:
                    Assert.fail("Unexpected parameter: \"" + eachParam.getName() + "\"");
                    break;
            }
        }
    }

    @Test
    public void testParseUrl_Issue_2781()
    {
        final URLInfo info = UrlUtils.parseUrlString("http://fonts.googleapis.com/css?family=Roboto:400,300,500,300italic|Inconsolata:400,700");
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals(null, info.getUserInfo());
        Assert.assertEquals("fonts.googleapis.com", info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("/css", info.getPath());
        Assert.assertEquals("family=Roboto:400,300,500,300italic|Inconsolata:400,700", info.getQuery());
        Assert.assertEquals(null, info.getFragment());
    }

    @Test
    public void testRemoveUserInfo_NullOrBlank()
    {
        final String message = "Blank or null input string should return null";
        Assert.assertNull(message, UrlUtils.removeUserInfo((String) null));
        Assert.assertNull(message, UrlUtils.removeUserInfo(""));
        Assert.assertNull(message, UrlUtils.removeUserInfo("    "));
    }

    @Test
    public void testRemoveUserInfo_NoUserInfo()
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_NoUserInfo_IPv4()
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://192.0.2.100:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_NoUserInfo_IPv6()
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameOnly()
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe@anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameOnly_IPv4()
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe@192.0.2.100:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameOnly_IPv6()
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_EmptyPassword()
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:@anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_EmptyPassword_IPv4()
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:@192.0.2.100:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_EmptyPassword_IPv6()
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameAndPassword()
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:secret@anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameAndPassword_IPv4()
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:secret@192.0.2.100:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameAndPassword_IPv6()
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:secret@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_URLNull()
    {
        Assert.assertNull("Null input URL should return null", UrlUtils.removeUserInfo((URL) null));
    }

    @Test
    public void testRemoveUserInfo_URLNoUserInfo() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLNoUserInfo_IPv4() throws Throwable
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://192.0.2.100:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLNoUserInfo_IPv6() throws Throwable
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameOnly() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameOnly_IPv4() throws Throwable
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe@192.0.2.100:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameOnly_IPv6() throws Throwable
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLEmptyPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLEmptyPassword_IPv4() throws Throwable
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:@192.0.2.100:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLEmptyPassword_IPv6() throws Throwable
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameAndPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:secret@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameAndPassword_IPv4() throws Throwable
    {
        Assert.assertEquals("https://192.0.2.100:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:secret@192.0.2.100:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameAndPassword_IPv6() throws Throwable
    {
        Assert.assertEquals("https://[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:secret@[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:12345/some/path?foo=bar#fragment")));
    }
}
