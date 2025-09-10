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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import org.htmlunit.util.Cookie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import util.JUnitParamsUtils;
import util.JUnitParamsUtils.BlankStringParamProvider;

@RunWith(JUnitParamsRunner.class)
public class CookieJarImplTest
{
    @Test
    @Parameters(source = BlankStringParamProvider.class)
    public void toOkHttpCookie_BlankDomain(final String domain)
    {
        callToOkHttpCookieButExpectedException(new Cookie(domain, "name", "value", "/", null, false, false),
                                               CookieJarImpl.ERROR_MSG_DOMAIN);
    }

    @Test
    @Parameters(source = DomainDataProvider.class)
    public void toOkHttpCookie_ValidDomain(final String domain, final boolean expectedHostOnly)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(domain, "name", "value", "/", null, false, false));

        Assert.assertEquals("example.com", okCookie.domain());
        Assert.assertEquals(expectedHostOnly, okCookie.hostOnly());
    }

    @Test
    @Parameters(source = BlankStringParamProvider.class)
    public void toOkHttpCookie_BlankName(final String name)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie("domain", name, "value", "/", null, false, false));

        Assert.assertEquals("", okCookie.name());
    }

    @Test
    @Parameters(source = NameProvider.class)
    public void toOkHttpCookie_ValidName(final String name)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie("domain", name, "value", "/", null, false, false));

        Assert.assertEquals("name", okCookie.name());
    }

    @Test
    @Parameters(source = BlankStringParamProvider.class)
    public void toOkHttpCookie_BlankValue(final String name)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie("domain", name, "value", "/", null, false, false));

        Assert.assertEquals("", okCookie.name());
    }

    @Test
    @Parameters(source = ValueProvider.class)
    public void toOkHttpCookie_ValidValue(final String value)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie("domain", "name", value, "/", null, false, false));

        Assert.assertEquals("value", okCookie.value());
    }

    @Test
    @Parameters(source = BlankStringParamProvider.class)
    public void toOkHttpCookie_BlankPath(final String path)
    {
        callToOkHttpCookieButExpectedException(new Cookie("domain", "name", "value", path, null, false, false),
                                               CookieJarImpl.ERROR_MSG_PATH);
    }

    @Test
    @Parameters(source = PathProvider.class)
    public void toOkHttpCookie_ValidPath(final String path)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie("domain", "name", "value", path, null, false, false));

        Assert.assertEquals("/path", okCookie.path());
    }

    @Test
    public void toOkHttpCookie_InvalidPath()
    {
        callToOkHttpCookieButExpectedException(new Cookie("domain", "name", "value", "path_not_starting_with_slash", null, false, false),
                                               CookieJarImpl.ERROR_MSG_PATH);
    }

    private void callToOkHttpCookieButExpectedException(final Cookie huCookie, final String expectedMessagePrefix)
    {
        try
        {
            CookieJarImpl.toOkHttpCookie(huCookie);
            Assert.fail("Expected IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            Assert.assertTrue("Unexpected exception message", e.getMessage().startsWith(expectedMessagePrefix));
        }
    }

    public static class DomainDataProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.parseParamSets("example.com|true",      //
                                                     " example.com |true",    //
                                                     ".example.com|false",    //
                                                     " .example.com |false");
        }
    }

    public static class NameProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.parseParamSets("name", " name ");
        }
    }

    public static class ValueProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.parseParamSets("value", " value ");
        }
    }

    public static class PathProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.parseParamSets("/path", " /path ");
        }
    }
}
