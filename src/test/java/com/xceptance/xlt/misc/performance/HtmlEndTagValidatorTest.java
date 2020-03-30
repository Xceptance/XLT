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
package com.xceptance.xlt.misc.performance;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Tests the performance of the implementation of {@link HtmlEndTagValidator#validate(String)}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HtmlEndTagValidatorTest extends AbstractHtmlTest
{
    /**
     * Content of a small HTML page as string.
     */
    private static final String smallPageContent = "<html><head><title></title>\t\r\n</head><body> \n\t</body>\n\r</html>\n<!-- \nComment \t-->\n <!-- Test -->";

    /**
     * Content of big HTML page.
     */
    private HtmlPage htmlPage;

    /**
     * Number of iterations.
     */
    private static final int rounds = 10000;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed
     */
    @Before
    public void setUp() throws Exception
    {
        // read in resource's content and construct page
        htmlPage = setUp(this);
    }

    /**
     * Test the performance of {@link HtmlEndTagValidator#validate(String)} by passing a small HTML page.
     * 
     * @throws Exception
     *             thrown when something went wrong
     */
    @Test
    @Ignore("Performance test")
    public final void testValidate_SpeedSmallPage() throws Exception
    {
        // dry run to give engine a chance to cache something
        HtmlEndTagValidator.getInstance().validate(smallPageContent);

        final long startTime = TimerUtils.getTime();
        for (int i = 0; i < rounds; i++)
        {
            HtmlEndTagValidator.getInstance().validate(smallPageContent);
        }
        final long endTime = TimerUtils.getTime();
        final long duration = endTime - startTime;
        final double rate = ((double) rounds) / ((double) duration);
        XltLogger.runTimeLogger.info(String.format("Duration for %d iterations using a small page: %dms (%.2f pages per ms).", rounds,
                                                   duration, rate));

    }

    /**
     * Test the performance of {@link HtmlEndTagValidator#validate(String)} by passing a big HTML page.
     * 
     * @throws Exception
     *             thrown when something went wrong
     */
    @Test
    @Ignore("Performance test")
    public final void testValidate_SpeedBig() throws Exception
    {
        final String content = htmlPage.getWebResponse().getContentAsString();

        // dry run to give engine a chance to cache something
        HtmlEndTagValidator.getInstance().validate(content);

        final long startTime = TimerUtils.getTime();
        for (int i = 0; i < rounds; i++)
        {
            HtmlEndTagValidator.getInstance().validate(content);
        }
        final long endTime = TimerUtils.getTime();
        final long duration = endTime - startTime;
        final double rate = ((double) rounds) / ((double) duration);
        XltLogger.runTimeLogger.info(String.format("Duration for %d iterations using a big page: %dms (%.2f pages per ms).", rounds,
                                                   duration, rate));
    }
}
