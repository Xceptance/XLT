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

import java.time.Instant;
import java.util.Date;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.htmlunit.util.Cookie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.internal.http.DatesKt;
import util.JUnitParamsUtils;
import util.JUnitParamsUtils.BlankStringParamProvider;

@RunWith(JUnitParamsRunner.class)
public class CookieJarImplTest
{
    private static final String DOMAIN = "domain";

    private static final String NAME = "name";

    private static final String VALUE = "value";

    private static final String PATH = "/";

    private static final Date EXPIRES_AT = null;

    private static final boolean SECURE = false;

    private static final boolean HTTP_ONLY = false;

    @Test
    @Parameters(source = InvalidDomainProvider.class)
    public void toOkHttpCookie_Domain_Invalid(final String domain)
    {
        callToOkHttpCookieButExpectException(new Cookie(domain, NAME, VALUE, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));
    }

    @Test
    @Parameters(
        {
            "example.com|true", ".example.com|false"
    })
    public void toOkHttpCookie_Domain_Valid(final String domain, final boolean expectedHostOnly)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(domain, NAME, VALUE, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));

        Assert.assertEquals("example.com", okCookie.domain());
        Assert.assertEquals(expectedHostOnly, okCookie.hostOnly());
    }

    @Test
    @Parameters(source = InvalidNameProvider.class)
    public void toOkHttpCookie_Name_Invalid(final String name)
    {
        callToOkHttpCookieButExpectException(new Cookie(DOMAIN, name, VALUE, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));
    }

    @Test
    @Parameters(
        {
            NAME, ""
    })
    public void toOkHttpCookie_Name_Valid(final String name)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, name, VALUE, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));

        Assert.assertEquals(name, okCookie.name());
    }

    @Test
    @Parameters(source = InvalidValueProvider.class)
    public void toOkHttpCookie_Value_Invalid(final String value)
    {
        callToOkHttpCookieButExpectException(new Cookie(DOMAIN, NAME, value, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));
    }

    @Test
    @Parameters(
        {
            VALUE, ""
    })
    public void toOkHttpCookie_Value_Valid(final String value)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, NAME, value, PATH, EXPIRES_AT, SECURE, HTTP_ONLY));

        Assert.assertEquals(value, okCookie.value());
    }

    @Test
    @Parameters(source = InvalidPathProvider.class)
    public void toOkHttpCookie_Path_Invalid(final String path)
    {
        callToOkHttpCookieButExpectException(new Cookie(DOMAIN, NAME, VALUE, path, EXPIRES_AT, SECURE, HTTP_ONLY));
    }

    @Test
    @Parameters(
        {
            PATH, "/path", "/path/path"
    })
    public void toOkHttpCookie_Path_Valid(final String path)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, NAME, VALUE, path, EXPIRES_AT, SECURE, HTTP_ONLY));

        Assert.assertEquals(path, okCookie.path());
    }

    @Test
    public void toOkHttpCookie_Path_Null()
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, NAME, VALUE, null, EXPIRES_AT, SECURE, HTTP_ONLY));

        Assert.assertEquals(PATH, okCookie.path());
    }

    @Test
    public void toOkHttpCookie_ExpiresAt_Null()
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, NAME, VALUE, PATH, null, SECURE, HTTP_ONLY));

        Assert.assertEquals(DatesKt.MAX_DATE, okCookie.expiresAt());
    }

    @Test
    @Parameters(source = ExpiresAtProvider.class)
    public void toOkHttpCookie_ExpiresAt_Valid(final Date expiresAt)
    {
        final var okCookie = CookieJarImpl.toOkHttpCookie(new Cookie(DOMAIN, NAME, VALUE, PATH, expiresAt, SECURE, HTTP_ONLY));

        Assert.assertEquals(expiresAt.getTime(), okCookie.expiresAt());
    }

    // --- Helpers and data providers ---

    private void callToOkHttpCookieButExpectException(final Cookie huCookie)
    {
        final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                                                                       () -> CookieJarImpl.toOkHttpCookie(huCookie));
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.startsWith(CookieJarImpl.ERROR_MSG_COOKIE_INVALID));
    }

    public static class InvalidDomainProvider extends BlankStringParamProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.wrapEachParam(".", " example.com ", " .example.com ");
        }
    }

    public static class InvalidNameProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.wrapEachParam(" ", " name", "name ", " name ");
        }
    }

    public static class InvalidValueProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.wrapEachParam(" ", " value", "value ", " value ");
        }
    }

    public static class InvalidPathProvider extends BlankStringParamProvider
    {
        public static Object[][] provide()
        {
            return JUnitParamsUtils.wrapEachParam("path", " /path ");
        }
    }

    public static class ExpiresAtProvider
    {
        public static Object[][] provide()
        {
            final Instant now = Instant.now();

            return JUnitParamsUtils.wrapEachParam(new Date(), new Date(now.minusSeconds(1000).toEpochMilli()),
                                                  new Date(now.plusSeconds(1000).toEpochMilli()));
        }
    }
}
