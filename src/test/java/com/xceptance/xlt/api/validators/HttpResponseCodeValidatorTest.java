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
package com.xceptance.xlt.api.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Tests the implementation of {@link HttpResponseCodeValidator}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class HttpResponseCodeValidatorTest
{
    /**
     * Singleton (default) instance.
     */
    private final HttpResponseCodeValidator instance = HttpResponseCodeValidator.getInstance();

    private HtmlPage htmlPage = null;

    @Before
    public void intro() throws Throwable
    {
        final WebWindow w = Mockito.mock(WebWindow.class);
        Mockito.doReturn(w).when(w).getTopWindow();
        final XltWebClient wc = new XltWebClient();
        wc.setTimerName("MyTimer");
        Mockito.doReturn(wc).when(w).getWebClient();
        final WebResponse r = Mockito.mock(WebResponse.class);

        htmlPage = new HtmlPage(r, w);
        Mockito.doReturn(htmlPage).when(w).getEnclosedPage();
    }

    @Test(expected = AssertionError.class)
    public void validateLWPage_InvalidResponseCode() throws Exception
    {
        final LightWeightPage page = Mockito.mock(LightWeightPage.class);
        Mockito.doReturn(404).when(page).getHttpResponseCode();

        instance.validate(page);
    }

    @Test(expected = AssertionError.class)
    public void validateLWPage_NotConfiguredResponseCode() throws Exception
    {
        final LightWeightPage page = Mockito.mock(LightWeightPage.class);
        Mockito.doReturn(200).when(page).getHttpResponseCode();

        new HttpResponseCodeValidator(404).validate(page);
    }

    @Test
    public void validateLWPage_ValidResponseCode() throws Exception
    {
        final LightWeightPage page = Mockito.mock(LightWeightPage.class);
        Mockito.doReturn(200).when(page).getHttpResponseCode();

        instance.validate(page);
    }

    @Test
    public void validateLWPage_ConfiguredResponseCode() throws Exception
    {
        final LightWeightPage page = Mockito.mock(LightWeightPage.class);
        Mockito.doReturn(500).when(page).getHttpResponseCode();

        new HttpResponseCodeValidator(500).validate(page);
    }

    @Test(expected = AssertionError.class)
    public void validateXltHtmlPage_InvalidResponseCode() throws Exception
    {
        final WebResponse r = htmlPage.getWebResponse();
        Mockito.doReturn(404).when(r).getStatusCode();

        instance.validate(htmlPage);
    }

    @Test(expected = AssertionError.class)
    public void validateXltHtmlPage_NotConfiguredResponseCode() throws Exception
    {
        final WebResponse r = htmlPage.getWebResponse();
        Mockito.doReturn(200).when(r).getStatusCode();

        new HttpResponseCodeValidator(404).validate(htmlPage);
    }

    @Test
    public void validateXltHtmlPage_ValidResponseCode() throws Exception
    {
        final WebResponse r = htmlPage.getWebResponse();
        Mockito.doReturn(200).when(r).getStatusCode();

        instance.validate(htmlPage);
    }

    @Test
    public void validateXltHtmlPage_ConfiguredResponseCode() throws Exception
    {
        final WebResponse r = htmlPage.getWebResponse();
        Mockito.doReturn(500).when(r).getStatusCode();

        new HttpResponseCodeValidator(500).validate(htmlPage);
    }

    @Test
    public void testResponseCode_Default()
    {
        Assert.assertEquals(200, HttpResponseCodeValidator.getInstance().getHttpResponseCode());
    }

    @Test
    public void testResponseCode_Custom()
    {
        Assert.assertEquals(404, new HttpResponseCodeValidator(404).getHttpResponseCode());
    }

    @Test
    public void testEquals_Same()
    {
        Assert.assertTrue(HttpResponseCodeValidator.getInstance().equals(HttpResponseCodeValidator.getInstance()));
        Assert.assertTrue(new HttpResponseCodeValidator(500).equals(new HttpResponseCodeValidator(500)));
    }

    @Test
    public void testEquals_Different()
    {
        Assert.assertFalse(new HttpResponseCodeValidator(404).equals(new HttpResponseCodeValidator(500)));
        Assert.assertFalse(new HttpResponseCodeValidator(404).equals("500"));
    }

    @Test
    public void testHashcode_Same()
    {
        Assert.assertEquals(new HttpResponseCodeValidator(404).hashCode(), new HttpResponseCodeValidator(404).hashCode());
        Assert.assertEquals(new HttpResponseCodeValidator(200).hashCode(), HttpResponseCodeValidator.getInstance().hashCode());
    }

    @Test
    public void testHashcode_Different()
    {
        Assert.assertNotSame(new HttpResponseCodeValidator(404).hashCode(), new HttpResponseCodeValidator(301).hashCode());
        Assert.assertNotSame(new Object().hashCode(), HttpResponseCodeValidator.getInstance().hashCode());
        // finally, check that hashcode doesn't rely on response code solely
        Assert.assertNotSame(200, HttpResponseCodeValidator.getInstance().hashCode());
    }
}
