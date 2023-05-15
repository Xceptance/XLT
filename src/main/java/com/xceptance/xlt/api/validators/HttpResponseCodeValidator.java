/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.htmlunit.LightWeightPage;

/**
 * This class validates response codes and can be used as an instance (constructor) or as a global instance for easy
 * reuse by calling {@link HttpResponseCodeValidator#getInstance()}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HttpResponseCodeValidator
{
    /**
     * The response code to validate against.
     */
    private final int httpResponseCode;

    /**
     * Constructor.
     * 
     * @param httpResponseCode
     *            the expected response code
     */
    public HttpResponseCodeValidator(final int httpResponseCode)
    {
        this.httpResponseCode = httpResponseCode;
    }

    /**
     * Constructor, using 200 as response code for validation.
     */
    public HttpResponseCodeValidator()
    {
        this(200);
    }

    /**
     * Validates the specified HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final HtmlPage page)
    {
        Assert.assertNotNull("No html page available to validate the response code against", page);
        Assert.assertEquals("Response code does not match", httpResponseCode, page.getWebResponse().getStatusCode());
    }

    /**
     * Validates the specified lightweight HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final LightWeightPage page)
    {
        Assert.assertNotNull("No html page available to validate the response code against", page);
        Assert.assertEquals("Response code does not match", httpResponseCode, page.getHttpResponseCode());
    }

    /**
     * Returns the set response code.
     * 
     * @return the set response code
     */
    public int getHttpResponseCode()
    {
        return httpResponseCode;
    }

    /**
     * Returns an instance of this class with 200 as the response code against which to validate.
     * 
     * @return an instance of HttpResponseCodeValidator with 200 as set response code
     */
    public static HttpResponseCodeValidator getInstance()
    {
        return HttpResponseCodeValidator_Singleton.instance;
    }

    /**
     * Singleton implementation of {@link HttpResponseCodeValidator}.
     */
    private static class HttpResponseCodeValidator_Singleton
    {
        /**
         * Singleton instance.
         */
        private static final HttpResponseCodeValidator instance;

        // static initializer (synchronized by class loader)
        static
        {
            instance = new HttpResponseCodeValidator();
        }
    }

    /**
     * Checks if this validator is equal to another one. It is equal, when it supports the same response code.
     * 
     * @return true if both validators check for the same response code, false otherwise
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        final HttpResponseCodeValidator other = (HttpResponseCodeValidator) obj;
        return httpResponseCode == other.httpResponseCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ((getClass().hashCode() & 0xffff) << 16) ^ (httpResponseCode & 0xffff);
    }
}
