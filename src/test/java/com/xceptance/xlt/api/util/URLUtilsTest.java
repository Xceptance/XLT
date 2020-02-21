package com.xceptance.xlt.api.util;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test the implementation of {@link URLUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class URLUtilsTest extends AbstractXLTTestCase
{
    /**
     * Base URL.
     */
    protected static final String BASE_URL = "http://localhost/some-app";

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by using a javascript function call
     * as relative target URL.
     */
    @Test
    public void testMakeLinkAbsolute_JSLink()
    {
        final String jsURL = "javascript:addToBasket()";

        Assert.assertNull(URLUtils.makeLinkAbsolute(BASE_URL, jsURL));
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by using an email address link as
     * relative target URL.
     */
    @Test
    public void testMakeLinkAbsolute_MailToLink()
    {
        final String mailToLink = "mailto:john@doe.com";

        Assert.assertNull(URLUtils.makeLinkAbsolute(BASE_URL, mailToLink));
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by passing a non-javascript link,
     * and non-mail respectively, as relative target URL.
     */
    @Test
    public void testMakeLinkAbsolute_ValidURL()
    {
        final String relURL = "some-subdir/../index.html";

        final String result = URLUtils.makeLinkAbsolute(BASE_URL, relURL);
        Assert.assertNotNull(result);
        Assert.assertEquals(BASE_URL.substring(0, BASE_URL.indexOf("some-app")) + "index.html", result);
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by passing an empty URL.
     */
    @Test
    public void testMakeLinkAbsolute_EmptyURL()
    {
        final String relURL = StringUtils.EMPTY;

        final String result = URLUtils.makeLinkAbsolute(BASE_URL, relURL);
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by passing an invalid base URL.
     */
    @Test
    public void testMakeLinkAbsolute_InvalidBaseURL()
    {
        final String baseURL = "http://some host.somedomain.com";
        final String targetURL = "someTarget.html";

        Assert.assertNull(URLUtils.makeLinkAbsolute(baseURL, targetURL));
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(String, String)} by passing an invalid target URL.
     */
    @Test
    public void testMakeLinkAbsolute_InvalidTargetURL()
    {
        final String targetURL = "http:#dir/index.html";

        Assert.assertNull(URLUtils.makeLinkAbsolute(BASE_URL, targetURL));
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(URI, String)} by passing a null reference as URL
     * string.
     */
    @Test
    public void testMakeLinkAbsolute_UrlStringIsNull()
    {
        Assert.assertNull(URLUtils.makeLinkAbsolute(BASE_URL, null));
    }

    /**
     * Tests the implementation of {@link URLUtils#makeLinkAbsolute(URI, String)} by passing a base URI with no path.
     */
    @Test
    public void testMakeLinkAbsolute_MissingPathInBaseURI()
    {
        Assert.assertEquals("http://www.xceptance.de/produkte", URLUtils.makeLinkAbsolute("http://www.xceptance.de", "produkte"));
    }
}
