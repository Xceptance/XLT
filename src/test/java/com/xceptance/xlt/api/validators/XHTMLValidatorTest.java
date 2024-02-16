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

import org.htmlunit.BrowserVersion;
import org.htmlunit.MockWebConnection;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltEngine;

/**
 * Test the implementation of {@link XHTMLValidator} to ensure proper behavior.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class XHTMLValidatorTest
{
    /**
     * Well formed XHTML.
     */
    private final static String wellformedXHTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
                                                  + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                                  + "<head>"
                                                  + "   <title>A test title</title>"
                                                  + "</head>"
                                                  + "<body>"
                                                  + "   <div id=\"container\">"
                                                  + "       <h1>Test</h1>" + "   </div>" + "</body>" + "</html>";

    /**
     * Erroneous XHTML.
     */
    private final static String errorXHTML_NoHead = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                                                    + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                                    + "   <title>A test title</title>"
                                                    + "</head>"
                                                    + "<body>"
                                                    + "<table summary=\"none\">"
                                                    + "   <tr>"
                                                    + "       <form action=\"\">"
                                                    + "           <td>yyy</td>"
                                                    + "       </form>" + "   </tr>" + "</table>" + "</body>" + "</html>";

    /**
     * Erroneous XHTML, wrong nesting.
     */
    private final static String errorXHTML_WrongNesting = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                                                          + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                                          + "<head>"
                                                          + "   <title>A test title</title>"
                                                          + "</head>"
                                                          + "<body>"
                                                          + "<table summary=\"none\">"
                                                          + "   <tr>"
                                                          + "       <b><i></b></i>"
                                                          + "   </tr>"
                                                          + "</table>" + "</body>" + "</html>";

    private final static String DOCTYPE_XHTML10_STRICT = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    private final static String DOCTYPE_XHTML10_TRANSITIONAL = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

    private final static String DOCTYPE_XHTML10_FRAMESET = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">";

    private final static String DOCTYPE_XHTML11_STRICT = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";

    private final static String DOCTYPE_XHTML10_BASIC = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.0//EN\" \"http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd\">";

    private final static String DOCTYPE_XHTML11_BASIC = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\" \"http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd\">";

    /**
     * Well formed XHTML.
     */
    private final static String wellformedXHTML_NoDocType = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">" + "<head>"
                                                            + "   <title>A test title</title>" + "</head>" + "<body>"
                                                            + "   <div id=\"container\">" + "       <h1>Test</h1>" + "   </div>"
                                                            + "</body>" + "</html>";

    private final static String wellformedXHTMLFrameset_NoDocType = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                                                    + "<head>" + "   <title>A test title</title>" + "</head>"
                                                                    + "<frameset>" + "   <frame name=\"test\" src=\"test.html\" />"
                                                                    + "</frameset>" + "</html>";

    /**
     * Test fixture setup.
     */
    @Before
    public void setProperty()
    {
        // we want output, so set the property.
        XltProperties.getInstance().setProperty(XHTMLValidator.PROPERTY_NAME, "true");
    }

    /**
     * Test clean-up.
     */
    @After
    public void cleanUp()
    {
        XltEngine.reset();
    }

    /**
     * Test the default handling and well formed HTML.
     */
    @Test
    public final void wellformedXHTML()
    {
        try
        {
            (XHTMLValidator.getInstance()).validate(wellformedXHTML);
        }
        catch (final Exception e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test the default handling and HTML with errors.
     */
    @Test(expected = AssertionError.class)
    public final void errorXHTML_NoHead() throws Throwable
    {
        (XHTMLValidator.getInstance()).validate(errorXHTML_NoHead);
    }

    /**
     * Test the default handling and HTML with errors.
     */
    @Test(expected = AssertionError.class)
    public final void errorXHTML_WrongNesting() throws Throwable
    {
        (XHTMLValidator.getInstance()).validate(errorXHTML_WrongNesting);
    }

    /**
     * Test the default handling and HTML with errors.
     */
    @Test
    public final void errorNoBreakXHTML()
    {
        try
        {
            new XHTMLValidator(false, false).validate(errorXHTML_NoHead);
        }
        catch (final Exception e)
        {
            Assert.fail("Error came up, but not expected." + e.getMessage());
        }
    }

    /**
     * Test the default handling and HTML with errors.
     */
    @Test
    public final void xhtmlTest()
    {
        final XHTMLValidator v = XHTMLValidator.getInstance();

        try
        {
            v.validate(DOCTYPE_XHTML10_STRICT + wellformedXHTML_NoDocType);
            v.validate(DOCTYPE_XHTML10_TRANSITIONAL + wellformedXHTML_NoDocType);
            v.validate(DOCTYPE_XHTML10_FRAMESET + wellformedXHTMLFrameset_NoDocType);
            v.validate(DOCTYPE_XHTML11_STRICT + wellformedXHTML_NoDocType);
            v.validate(DOCTYPE_XHTML10_BASIC + wellformedXHTML_NoDocType);
            v.validate(DOCTYPE_XHTML11_BASIC + wellformedXHTML_NoDocType);
        }
        catch (final Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testValidateHtmlPage() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse(wellformedXHTML);
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");

            XHTMLValidator.getInstance().validate(page);
        }
    }
}
