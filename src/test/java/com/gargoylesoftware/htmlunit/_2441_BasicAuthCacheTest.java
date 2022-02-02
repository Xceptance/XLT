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
package com.gargoylesoftware.htmlunit;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * Tests the fix for issue #2441.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2441_BasicAuthCacheTest
{
    private WebClient wc;

    @Before
    public void makeClient()
    {
        wc = new WebClient();
    }

    @After
    public void shutdownClient()
    {
        wc.close();
        wc = null;
    }

    @Test
    public void test() throws Throwable
    {
        final AuthCache cache = ReflectionUtils.readField(HttpWebConnection.class, wc.getWebConnection(), "sharedAuthCache_");
        final BasicScheme authScheme = new BasicScheme();

        final Header header = new BasicHeader(AUTH.PROXY_AUTH, "Basic realm=\"Linux Class\"");

        authScheme.processChallenge(header);

        Assert.assertTrue("Scheme not complete", authScheme.isComplete());
        Assert.assertTrue("Scheme is not authenticating against proxy", authScheme.isProxy());

        cache.put(new HttpHost("example.org", 8080, "http"), authScheme);

        final AuthScheme cachedAuthScheme = cache.get(new HttpHost("example.org", 8080, "http"));

        Assert.assertTrue("Unexpected authentication scheme",cachedAuthScheme instanceof BasicScheme);

        Assert.assertTrue("Scheme not complete", cachedAuthScheme.isComplete());
        Assert.assertTrue("Scheme is not authenticating against proxy",        ((BasicScheme) cachedAuthScheme).isProxy());

    }
}
