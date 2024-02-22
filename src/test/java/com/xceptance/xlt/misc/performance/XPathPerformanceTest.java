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
package com.xceptance.xlt.misc.performance;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Check the best use of XPath expressions on a document.
 *
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class XPathPerformanceTest extends AbstractHtmlTest
{
    /**
     * HTML page used for test.
     */
    private HtmlPage htmlPage;

    /**
     * Test fixture setup.
     *
     * @throws Exception
     *             thrown when setup failed
     */
    @Before
    public void setUp() throws Exception
    {
        htmlPage = setUp(this);
    }

    /**
     * Runs the speed test.
     *
     * @throws Exception
     *             thrown when something went wrong
     */
    @Test
    @Ignore("Performance test")
    public void testPerformance() throws Exception
    {
        final int count = 5000;
        // html > body > div #doc > div #container > div #content > form #addressForm
        final String fullXPath = "/html/body/div[@id='doc']/div[@id='container']/div[@id='content']/form[@id='addressForm']";
        final String fullXPathNoIDs = "/html/body/div/div/div/form[@id='addressForm']";
        final String allUnderBodyXPath = "//div[@id='doc']/div[@id='container']/div[@id='content']/form[@id='addressForm']";
        final String allFormsXPath = "//form[@id='addressForm']";
        final String byIDFunction = "id('addressForm')";

        final String contentXPath = "/html/body/div[@id='doc']/div[@id='container']/div[@id='content']";
        final String onlyThisFormXPath = "./form[@id='addressForm']";

        final long s1 = TimerUtils.get().getStartTime();
        List<?> list1 = null;
        for (int i = 0; i < count; i++)
        {
            list1 = htmlPage.getByXPath(fullXPath);
        }
        final long e1 = TimerUtils.get().getElapsedTime(s1);

        final long s2 = TimerUtils.get().getStartTime();
        List<?> list2 = null;
        for (int i = 0; i < count; i++)
        {
            list2 = htmlPage.getByXPath(allUnderBodyXPath);
        }
        final long e2 = TimerUtils.get().getElapsedTime(s2);

        final long s3 = TimerUtils.get().getStartTime();
        List<?> list3 = null;
        for (int i = 0; i < count; i++)
        {
            list3 = htmlPage.getByXPath(allFormsXPath);
        }
        final long e3 = TimerUtils.get().getElapsedTime(s3);

        final long s4 = TimerUtils.get().getStartTime();
        List<?> list4 = null;
        for (int i = 0; i < count; i++)
        {
            list4 = htmlPage.getByXPath(fullXPathNoIDs);
        }
        final long e4 = TimerUtils.get().getElapsedTime(s4);

        final long s5 = TimerUtils.get().getStartTime();
        final List<?> content = htmlPage.getByXPath(contentXPath);
        final HtmlElement element = (HtmlElement) content.get(0);
        List<?> list5 = null;
        for (int i = 0; i < count; i++)
        {
            list5 = element.getByXPath(onlyThisFormXPath);
        }
        final long e5 = TimerUtils.get().getElapsedTime(s5);

        final long s6 = TimerUtils.get().getStartTime();
        HtmlElement id = null;
        for (int i = 0; i < count; i++)
        {
            id = htmlPage.getHtmlElementById("addressForm");
        }
        final long e6 = TimerUtils.get().getElapsedTime(s6);

        final long s7 = TimerUtils.get().getStartTime();
        List<?> list7 = null;
        for (int i = 0; i < count; i++)
        {
            list7 = htmlPage.getByXPath(byIDFunction);
        }
        final long e7 = TimerUtils.get().getElapsedTime(s7);

        Assert.assertNotNull(list1);
        Assert.assertEquals(1, list1.size());
        Assert.assertNotNull(list2);
        Assert.assertEquals(1, list2.size());
        Assert.assertNotNull(list3);
        Assert.assertEquals(1, list3.size());
        Assert.assertNotNull(list4);
        Assert.assertEquals(1, list4.size());
        Assert.assertNotNull(list5);
        Assert.assertEquals(1, list5.size());
        Assert.assertNotNull(id);
        Assert.assertEquals(1, list7.size());
        System.out.println(formattedMsg("Full", e1, null));
        System.out.println(formattedMsg("All under body", e2, "- " + allUnderBodyXPath));
        System.out.println(formattedMsg("Only element", e3, "- " + allFormsXPath));
        System.out.println(formattedMsg("Less ids", e4, "- " + fullXPathNoIDs));
        System.out.println(formattedMsg("Query on result", e5, "- " + contentXPath + " && " + onlyThisFormXPath));
        System.out.println(formattedMsg("getHtmlElementById", e6, "- getHtmlElementById(\"addressForm\")"));
        System.out.println(formattedMsg("xpath id function", e7, "- " + byIDFunction));

    }

    private String formattedMsg(final String msg, final long duration, final String appendix)
    {
        return StringUtils.rightPad(msg, 20) + ":" + StringUtils.leftPad(duration + "ms ", 8) + ((appendix != null) ? appendix : "");
    }
}
