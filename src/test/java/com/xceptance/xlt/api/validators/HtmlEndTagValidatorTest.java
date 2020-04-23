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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test the implementation of {@link HtmlEndTagValidator}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HtmlEndTagValidatorTest extends AbstractXLTTestCase
{
    /**
     * Prefix used to assemble the 1st test page.
     */
    protected static final String prefix1 = "<html><head><title></title></head><body></body>";

    /**
     * Prefix used to assemble the 2nd test page.
     */
    protected static final String prefix2 = "<html><head><title></title>\t\r\n</head><body> \n\t</body>\n\r";

    /**
     * HtmlEndTagValidator test instance.
     */
    protected HtmlEndTagValidator instance = null;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void setUp() throws Exception
    {
        instance = HtmlEndTagValidator.getInstance();
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by using valid pages whose closing HTML
     * tag is followed by white spaces at most.
     */
    @Test
    public void testValidate_NormalEnd()
    {
        final String c1 = prefix1 + "</html>";
        final String c2 = prefix2 + "</html>\n\t\n\r";

        try
        {
            instance.validate(c1);
            instance.validate(c2);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by using an invalid page that doesn't
     * contain any closing HTML tag.
     */
    @Test
    public final void testValidate_NoEnd1()
    {
        testIllegalPageContent(prefix1);
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by using an invalid page that doesn't
     * contain any closing HTML tag.
     */
    @Test
    public final void testValidate_NoEnd2()
    {
        testIllegalPageContent(prefix2 + "<html>\n\t\n\r");
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing an invalid page whose closing
     * HTML tag is followed by regular text.
     */
    @Test
    public final void testValidate_TextAfterEnd1()
    {
        testIllegalPageContent(prefix1 + "asdfasdf asdf asf</html> hsdf ash");
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing an invalid page whose closing
     * HTML tag is followed by regular text.
     */
    @Test
    public final void testValidate_TextAfterEnd2()
    {
        testIllegalPageContent(prefix2 + "asdfasdf asdf asf</html>\n\n hsdf\t\n\r ash\n");
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing a page that contain multiple
     * closing HTML tags.
     */
    @Test
    public final void testValidate_DoubleEnd()
    {
        // no exception, we do not check for such cases yet
        final String c1 = prefix1 + "</html></html>";
        final String c2 = prefix2 + "</html>\n\t\n\r</html>";

        try
        {
            instance.validate(c1);
            instance.validate(c2);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing two pages whose closing HTML
     * tag is followed by HTML comments.
     */
    @Test
    public final void testValidate_CommentAfterEnd()
    {
        final String c1 = prefix1 + "</html><!-- Comment -->";
        final String c2 = prefix2 + "</html>\n<!-- \nComment \t-->\n <!-- Test -->";

        try
        {
            instance.validate(c1);
            instance.validate(c2);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing a page whose closing HTML tag
     * is followed by HTML non-comments.
     */
    @Test
    public final void testValidate_OtherTagAfterEnd1()
    {
        testIllegalPageContent(prefix1 + "<br></html>");
    }

    /**
     * Tests the implementation of {@link HtmlEndTagValidator#validate(String)} by passing a page whose closing HTML tag
     * is followed by HTML non-comments.
     */
    @Test
    public final void testValidate_OtherTagAfterEnd2()
    {
        testIllegalPageContent(prefix2 + "<br/></html>\n<!-- \nComment \t-->\n <!-- Test -->");
    }

    /**
     * @throws AssertionError
     */
    private void testIllegalPageContent(final String pageText) throws AssertionError
    {
        final String message = "HtmlEndTagValidator.validate(String) must throw an exception since given page content is invalid.";

        try
        {
            instance.validate(pageText);
            Assert.fail(message);
        }
        catch (final AssertionError e)
        {
            /* In this case we reached code that should not be reached (the Assert#fail from above) */
            if (message.equals(e.getMessage()))
            {
                throw e;
            }
        }
    }
}
