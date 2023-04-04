/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
    public void testParseUrl_OnlyProto() throws Throwable
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
    public void testParseUrl_ProtoHost() throws Throwable
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
    public void testParseUrl_UserInfoHost() throws Throwable
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
    public void testParseUrl_UserInfoOnly() throws Throwable
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
    public void testParseUrl_HostOnly() throws Throwable
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
    public void testParseUrl_HostPort() throws Throwable
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
    public void testParseUrl_PathOnly() throws Throwable
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
    public void testParseUrl_PathQuery() throws Throwable
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
    public void testParseUrl_FragmentOnly() throws Throwable
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
    public void testParseUrl_HostFragment() throws Throwable
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
    public void testParseUrl_QueryFragment() throws Throwable
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
    public void testParseUrl_PathFragment() throws Throwable
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
    public void testParseUrl_CompleteUrl() throws Throwable
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
    public void testUrlEncodedParametersFromNameValuePairs() throws Throwable
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
    public void testParseUrl_Issue_2781() throws Throwable
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
    public void testRemoveUserInfo_NullOrBlank() throws Throwable
    {
        final String message = "Blank or null input string should return null";
        Assert.assertNull(message, UrlUtils.removeUserInfo((String) null));
        Assert.assertNull(message, UrlUtils.removeUserInfo(""));
        Assert.assertNull(message, UrlUtils.removeUserInfo("    "));
    }

    @Test
    public void testRemoveUserInfo_NoUserInfo() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameOnly() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe@anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_EmptyPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:@anyserver.com:12345/some/path?foo=bar#fragment"));
    }

    @Test
    public void testRemoveUserInfo_UserNameAndPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo("https://johndoe:secret@anyserver.com:12345/some/path?foo=bar#fragment"));
    }
    @Test
    public void testRemoveUserInfo_URLNull() throws Throwable
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
    public void testRemoveUserInfo_URLUserNameOnly() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLEmptyPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

    @Test
    public void testRemoveUserInfo_URLUserNameAndPassword() throws Throwable
    {
        Assert.assertEquals("https://anyserver.com:12345/some/path?foo=bar#fragment",
                            UrlUtils.removeUserInfo(new URL("https://johndoe:secret@anyserver.com:12345/some/path?foo=bar#fragment")));
    }

}
