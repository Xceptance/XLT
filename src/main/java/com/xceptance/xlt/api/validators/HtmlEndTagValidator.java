/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Checks that a page has at least one closing HTML tag. Does not check, that this tag is the only one. It uses a
 * regular expression.
 * <p>
 * HTML comments are permitted after the closing HTML tag. Anything else will be logged as warning.
 * </p>
 * <p>
 * It also assumes a lower-case closing HTML tag according to the HTML/XHTML standard.
 * </p>
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HtmlEndTagValidator
{
    /**
     * Regular expression that matches the closing HTML tag followed by any content.
     */
    private static final String CL_HTML_REGEX = "(?ism)</(?:body|frameset)>(\\s|<!--.*?-->)*</html>(.*)$";

    /**
     * Regular expression used to check for regular trailing content.
     */
    private static final String REGULAR_TRAILING_CONTENT_REGEX = "(?sm)(\\s|<!--.*?-->)*";
    
    /**
     * Property name that controls the validator.
     */
    static final String PROPERTY_NAME = HtmlEndTagValidator.class.getName() + ".enabled";

    /**
     * The pattern to be use on the page.
     */
    private final Pattern pattern;

    /**
     * Pattern used to check for regular trailing content.
     */
    private final Pattern trailingContentPattern;

    /**
     * Constructor.
     * <p>
     * Declared as private to prevent external instantiation.
     * </p>
     */
    private HtmlEndTagValidator()
    {
        pattern = Pattern.compile(CL_HTML_REGEX);
        trailingContentPattern = Pattern.compile(REGULAR_TRAILING_CONTENT_REGEX);
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
        final String content = page.getWebResponse().getContentAsString();
        validate(content);
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
        final String content = page.getContent();
        validate(content);
    }

    /**
     * The validation as plain string method to be tested and used independently from the page.
     * 
     * @param content
     *            a snippet of HTML code to be checked
     */
    public void validate(final String content)
    {
        // parameter validation
        Assert.assertNotNull("The page content is null", content);
        Assert.assertTrue("The page is empty", content.length() > 0);

        // try a small size first to save time
        final String truncated = content.substring((int) (content.length() * 0.90));

        Matcher matcher = pattern.matcher(truncated);

        if (!matcher.find())
        {
            // try the long version
            matcher = pattern.matcher(content);
            Assert.assertTrue("html endtag not found", matcher.find());
        }

        final String trailingContent = matcher.group(2);
        matcher = trailingContentPattern.matcher(trailingContent);
        if (!matcher.matches())
        {
            XltLogger.runTimeLogger.warn("Only whitespace and XML comments are allowed after closing HTML tag.");
        }
    }

    /**
     * Returns the singleton instance.
     * 
     * @return the singleton instance
     */
    public static HtmlEndTagValidator getInstance()
    {
        final boolean enabled = XltProperties.getInstance().getProperty(PROPERTY_NAME, false);
        return enabled ? HtmlEndTagValidator_Singleton.instance : HtmlEndTagValidator_Singleton.noopInstance;
    }

    /**
     * Singleton implementation of {@link HtmlEndTagValidator}.
     */
    private static class HtmlEndTagValidator_Singleton
    {
        /**
         * Singleton instance.
         */
        private static final HtmlEndTagValidator instance;
        private static final HtmlEndTagValidator noopInstance;

        // static initializer (synchronized by class loader)
        static
        {
            instance = new HtmlEndTagValidator();
            noopInstance = new DisabledHtmlEndTagValidator();
        }
    }

   /**
    * NoOp implementation of the parent class.
    */
   private static final class DisabledHtmlEndTagValidator extends HtmlEndTagValidator
   {
       /** Does nothing. Validation is disabled. */
       @Override
       public void validate(final HtmlPage page)
       {
       };
       
       /** Does nothing. Validation is disabled. */
       @Override
       public void validate(final LightWeightPage page)
       {
       };
       
       /** Does nothing. Validation is disabled. */
       @Override
       public void validate(final String content)
       {
       };
   }
}
