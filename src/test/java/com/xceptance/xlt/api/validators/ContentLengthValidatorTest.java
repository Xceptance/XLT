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
package com.xceptance.xlt.api.validators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.net.HttpHeaderConstants;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Test the implementation of {@link ContentLengthValidator}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ContentLengthValidatorTest extends AbstractXLTTestCase
{
    /**
     * ContentLengthValidator test instance.
     */
    protected final ContentLengthValidator instance = ContentLengthValidator.getInstance();

    /**
     * Mocked lightweight page.
     */
    protected LightWeightPage page = null;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro() throws Exception
    {
        final WebResponse r = mock(WebResponse.class);
        Mockito.stub(r.getWebRequest()).toReturn(new WebRequest(new URL("http://localhost")));
        Mockito.doReturn("").when(r).getContentAsString();
        page = new LightWeightPageImpl(r, "Test", mock(XltWebClient.class));
    }

    /**
     * Tests the implementation of {@link ContentLengthValidator#validate(LightWeightPage)} by passing a page whose
     * content is encoded using the GZIP algorithm.
     */
    @Test
    public void testValidate_ContentGZipped()
    {
        Mockito.doReturn("iso-8859-1; " + HttpHeaderConstants.GZIP).when(page.getWebResponse())
               .getResponseHeaderValue(HttpHeaderConstants.CONTENT_ENCODING);

        try
        {
            instance.validate(page);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link ContentLengthValidator#validate(LightWeightPage)} by passing a page whose
     * response headers don't specify any content length.
     */
    @Test
    public void testValidate_NoContentLength()
    {
        try
        {
            instance.validate(page);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link ContentLengthValidator#validate(LightWeightPage)} by passing a page whose
     * response headers specify an invalid content length.
     */
    @Test
    public void testValidate_InvalidContentLength()
    {
        final int random = 1 + XltRandom.nextInt(100);
        // final byte[] content = getRandomBytes(random);
        Mockito.doReturn(Integer.toString(random)).when(page.getWebResponse()).getResponseHeaderValue(HttpHeaderConstants.CONTENT_LENGTH);
        Mockito.doReturn(null).when(page.getWebResponse()).getContentAsString();

        try
        {
            instance.validate(page);
            Assert.fail("ContentLengthValidator.validate(LightWeightPage) " + "must throw an exception since passed page is invalid.");
        }
        catch (final Throwable t)
        {
            // ignore
        }
    }

    /**
     * Tests the implementation of {@link ContentLengthValidator#validate(LightWeightPage)} by passing a page whose
     * response headers specify a valid content length.
     * 
     * @throws IOException
     */
    @Test
    public void testValidate_ValidContentLength() throws IOException
    {
        final int random = 1 + XltRandom.nextInt(100);
        final String content = RandomStringUtils.random(random);
        final byte[] contentArr = content.getBytes();

        Mockito.doReturn(Integer.toString(contentArr.length)).when(page.getWebResponse())
               .getResponseHeaderValue(HttpHeaderConstants.CONTENT_LENGTH);
        Mockito.doReturn(new ByteArrayInputStream(contentArr)).when(page.getWebResponse()).getContentAsStream();

        try
        {
            instance.validate(page);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }
}
