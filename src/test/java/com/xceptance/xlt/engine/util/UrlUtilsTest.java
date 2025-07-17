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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.http.client.utils.URLEncodedUtils;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;
import org.junit.runner.RunWith;
import util.JUnitParamsUtils;

/**
 * Tests the implementation of utility class {@link UrlUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@RunWith(JUnitParamsRunner.class)
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
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_ProtoHost(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("http://%s", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_UserInfoHost(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("john:doe@%s", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertEquals("john:doe", info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
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
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_HostOnly(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(host);
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("", info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_HostPort(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("%s:999", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
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
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_HostFragment(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("%s#fooBar", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
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
    @Parameters(method = "provideValidHosts")
    public void testParseUrl_CompleteUrl(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("http://john.doe:passwd@%s:999/myPath/1/2?foo=bar#fooBar", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
        Assert.assertEquals(999, info.getPort());
        Assert.assertEquals("/myPath/1/2", info.getPath());
        Assert.assertEquals("foo=bar", info.getQuery());
        Assert.assertEquals("fooBar", info.getFragment());
    }

    @Test
    @Parameters(value =
        {
            "",     // empty host
            "[]"    // empty IPv6 host
    })
    public void testParseUrl_CompleteUrl_OnlyDelimiters(final String host)
    {
        final URLInfo info = UrlUtils.parseUrlString(String.format("://@%s:/?#", host));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("", info.getProtocol());
        Assert.assertEquals("", info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals("/", info.getPath());
        Assert.assertEquals("", info.getQuery());
        Assert.assertEquals("", info.getFragment());
    }

    @Test
    @Parameters(method = "provideParametersForEmptyPortTest")
    public void testParseUrl_PortEmpty(final String host, final String port, final String path)
    {
        // test different cases where the port delimiter is present but no port is given
        // (e.g. "example.org:", "example.org: /abc")
        final URLInfo info = UrlUtils.parseUrlString(String.format("%s:%s%s", host, port, path));
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertNull(info.getProtocol());
        Assert.assertNull(info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
        Assert.assertEquals(-1, info.getPort());
        Assert.assertEquals(path, info.getPath());
        Assert.assertNull(info.getQuery());
        Assert.assertNull(info.getFragment());
    }

    @Test(expected = NumberFormatException.class)
    @Parameters(method = "provideUrlsWithInvalidPort")
    public void testParseUrl_PortCannotBeParsed(final String urlString)
    {
        UrlUtils.parseUrlString(urlString);
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRewriteUrl_PathOnly(final String host) throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl("/myPath/1/2", URLInfo.builder().proto("http").host(host).build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals(host, url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRewriteUrl_HostPath(final String host) throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl(String.format("%s/myPath/1/2", host), URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals(host, url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRewriteUrl_HostPortPath(final String host) throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl(String.format("%s:999/myPath/1/2", host), URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals(host, url.getHost());
        Assert.assertEquals("/myPath/1/2", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(999, url.getPort());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRewriteUrl_HostOnly(final String host) throws Throwable
    {
        final URL url = UrlUtils.rewriteUrl(host, URLInfo.builder().proto("http").build());
        Assert.assertNotNull(url);

        Assert.assertEquals("http", url.getProtocol());
        Assert.assertNull(url.getUserInfo());
        Assert.assertEquals(host, url.getHost());
        Assert.assertEquals("", url.getPath());
        Assert.assertNull(url.getQuery());
        Assert.assertNull(url.getRef());
        Assert.assertEquals(-1, url.getPort());
    }

    @Test
    @Parameters(value =
        {
            "example.org                                | foo.bar.test", //
            "192.0.2.100                                | 203.0.113.200", //
            "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]   | [2001:db8::1:0]", //
            "192.0.2.100                                | [2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", //
            "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]   | 192.0.2.100" //
    })
    public void testRewriteUrl_RewriteCompleteUrl(final String initialHost, final String overrideHost) throws Throwable
    {
        final String initialUrlString = String.format("http://john.doe:passwd@%s:999/myPath/1/2?foo=bar#fooBar", initialHost);
        final URLInfo urlOverride = URLInfo.builder().proto("https").userInfo("admin:pw").host(overrideHost).port(1111).path("/myPath/baz")
                                           .query("abc=xyz").fragment("abcXyz").build();

        final URL info = UrlUtils.rewriteUrl(initialUrlString, urlOverride);
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("https", info.getProtocol());
        Assert.assertEquals("admin:pw", info.getUserInfo());
        Assert.assertEquals(overrideHost, info.getHost());
        Assert.assertEquals("/myPath/baz", info.getPath());
        Assert.assertEquals("abc=xyz", info.getQuery());
        Assert.assertEquals("abcXyz", info.getRef());
        Assert.assertEquals(1111, info.getPort());
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRewriteUrl_EmptyOverride(final String host) throws Throwable
    {
        // override is empty, so the original URL should remain unchanged
        final String initialUrlString = String.format("http://john.doe:passwd@%s:999/myPath/1/2?foo=bar#fooBar", host);
        final URL info = UrlUtils.rewriteUrl(initialUrlString, URLInfo.builder().build());
        Assert.assertNotNull("Failed to parse url", info);

        Assert.assertEquals("http", info.getProtocol());
        Assert.assertEquals("john.doe:passwd", info.getUserInfo());
        Assert.assertEquals(host, info.getHost());
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
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_NoUserInfo(final String host)
    {
        final String urlString = String.format("https://%s:12345/some/path?foo=bar#fragment", host);
        Assert.assertEquals(urlString, UrlUtils.removeUserInfo(urlString));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_UserNameOnly(final String host)
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(String.format("https://johndoe@%s:12345/some/path?foo=bar#fragment", host)));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_EmptyPassword(final String host)
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(String.format("https://johndoe:@%s:12345/some/path?foo=bar#fragment", host)));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_UserNameAndPassword(final String host)
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(String.format("https://johndoe:secret@%s:12345/some/path?foo=bar#fragment", host)));
    }

    @Test
    public void testRemoveUserInfo_URLNull()
    {
        Assert.assertNull("Null input URL should return null", UrlUtils.removeUserInfo((URL) null));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_URLNoUserInfo(final String host) throws Throwable
    {
        final String urlString = String.format("https://%s:12345/some/path?foo=bar#fragment", host);
        Assert.assertEquals(urlString, UrlUtils.removeUserInfo(new URL(urlString)));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_URLUserNameOnly(final String host) throws Throwable
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(new URL(String.format("https://johndoe@%s:12345/some/path?foo=bar#fragment", host))));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_URLEmptyPassword(final String host) throws Throwable
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(new URL(String.format("https://johndoe:@%s:12345/some/path?foo=bar#fragment", host))));
    }

    @Test
    @Parameters(method = "provideValidHosts")
    public void testRemoveUserInfo_URLUserNameAndPassword(final String host) throws Throwable
    {
        Assert.assertEquals(String.format("https://%s:12345/some/path?foo=bar#fragment", host),
                            UrlUtils.removeUserInfo(new URL(String.format("https://johndoe:secret@%s:12345/some/path?foo=bar#fragment",
                                                                          host))));
    }

    /**
     * Test parameter provider method. Returns valid host names in different formats.
     */
    @SuppressWarnings("unused")
    private Object[] provideValidHosts()
    {
        return new Object[]
            {
                "example.org", //
                "192.0.2.100", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", //
                "[2001:db8::1:0]" //
            };
    }

    /**
     * Test parameter provider method. Returns host, port and path combinations for testing URLs where the port value is
     * missing (i.e. empty or whitespace-only).
     */
    @SuppressWarnings("unused")
    private Object[] provideParametersForEmptyPortTest()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("example.org", "", ""), //
                JUnitParamsUtils.wrapParams("example.org", " ", ""), //
                JUnitParamsUtils.wrapParams("example.org", "", "/abc"), //
                JUnitParamsUtils.wrapParams("example.org", " ", "/abc"), //
                JUnitParamsUtils.wrapParams("192.0.2.100", "", ""), //
                JUnitParamsUtils.wrapParams("192.0.2.100", " ", ""), //
                JUnitParamsUtils.wrapParams("192.0.2.100", "", "/abc"), //
                JUnitParamsUtils.wrapParams("192.0.2.100", " ", "/abc"), //
                JUnitParamsUtils.wrapParams("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", "", ""), //
                JUnitParamsUtils.wrapParams("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", " ", ""), //
                JUnitParamsUtils.wrapParams("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", "", "/abc"), //
                JUnitParamsUtils.wrapParams("[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]", " ", "/abc"), //
            };
    }

    /**
     * Test parameter provider method. Returns host port combinations with a valid host but a port that cannot be
     * parsed.
     */
    @SuppressWarnings("unused")
    private Object[] provideUrlsWithInvalidPort()
    {
        return JUnitParamsUtils.wrapEachParam(new Object[]
            {
                "example.org:123 ", //
                "example.org: 123", //
                "example.org:abc", //
                "example.org:abc999", //
                "example.org:9:99", //
                "example.org:999999999999", //
                "192.0.2.100:123 ", //
                "192.0.2.100: 123", //
                "192.0.2.100:abc", //
                "192.0.2.100:abc999", //
                "192.0.2.100:9:99", //
                "192.0.2.100:999999999999", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:123 ", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]: 123", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:abc", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:abc999", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:9:99", //
                "[2001:db8:1111:2222:aaaa:BBBB:1a2b:cd34]:999999999999" //
            });
    }
}
