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

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This validator groups the four most common validators into one to make the integration into the code easier.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class StandardValidator
{
    /**
     * The most common validators grouped into one validator to simplify the integration. It will validate the following
     * aspects:
     * <ul>
     * <li><em>response code</em>,</li>
     * <li><em>content length</em>,</li>
     * <li><em>closing HTML tag</em> and</li>
     * <li><em>JTidy</em></li>
     * </ul>
     * 
     * @param page
     *            the page to check
     */
    public void validate(final HtmlPage page) throws Exception
    {
        // response code = 200?
        HttpResponseCodeValidator.getInstance().validate(page);

        // does the length match?
        ContentLengthValidator.getInstance().validate(page);

        // is the page closed with </html>
        HtmlEndTagValidator.getInstance().validate(page);

        // use xhtml validator for a global conformity check
        XHTMLValidator.getInstance().validate(page);
    }

    /**
     * Returns the instance.
     * 
     * @return the singleton instance of this validator
     */
    public static StandardValidator getInstance()
    {
        return StandardValidator_Singleton._instance;
    }

    /**
     * Singleton implementation of {@link StandardValidator}.
     */
    private static class StandardValidator_Singleton
    {
        /**
         * Singleton instance.
         */
        private static final StandardValidator _instance;

        // static initializer (synchronized by class loader)
        static
        {
            _instance = new StandardValidator();
        }
    }
}
