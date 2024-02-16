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
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltEngine;

/**
 * Tests the implementation of {@link StandardValidator}.
 * 
 * @author sebastianloob
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        HtmlPage.class, WebResponse.class
    })
@PowerMockIgnore({"javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class StandardValidatorTest
{
    private StandardValidator validator;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro() throws Exception
    {
        // enable sub validators
        XltProperties.getInstance().setProperty(ContentLengthValidator.PROPERTY_NAME, "true");
        XltProperties.getInstance().setProperty(HtmlEndTagValidator.PROPERTY_NAME, "true");
        XltProperties.getInstance().setProperty(XHTMLValidator.PROPERTY_NAME, "true");

        validator = StandardValidator.getInstance();
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
     * Well formed XHTML.
     */
    private final static String htmlSource = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
                                             + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                             + "<head>"
                                             + "   <title>A test title</title>"
                                             + "</head>"
                                             + "<body>"
                                             + "   <div id=\"container\">"
                                             + "       <h1>Test</h1>" + "   </div>" + "</body>" + "</html>";

    /**
     * Verifies the correct implementation of {@link StandardValidator#validate(HtmlPage)}.
     * 
     * @throws Throwable
     */
    @Test
    public void testValidate() throws Throwable
    {
        try (final WebClient webClient = new WebClient())
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse(htmlSource);
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");
            validator.validate(page);
        }
    }

    /**
     * Verifies, that an exception is thrown, if the html page is {@code null}.
     * 
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testValidatePageIsNull() throws Exception
    {
        validator.validate((HtmlPage) null);
    }

    /**
     * Verifies, that an exception is thrown, if the html page is not closed with {@code </html>}.
     * 
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testValidatePageNotClosedWithHtmlTag() throws Exception
    {
        final String source = "<html>" + "<head>" + "</head>" + "<body>" + "</body>";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse(source);
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");
            validator.validate(page);
        }
    }
}
