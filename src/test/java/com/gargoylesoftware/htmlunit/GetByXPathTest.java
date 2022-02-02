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
package com.gargoylesoftware.htmlunit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.engine.util.TimerUtils;

public class GetByXPathTest extends AbstractHtmlUnitTest
{
    private final static int WARMUP_LOOPS = 500;

    private final static int GETBYXPATH_LOOPS = 100000;

    private WebClient webClient;

    private static final String domain = "mootools.net";

    private HtmlPage getHtmlPage() throws Exception
    {
        // won't go there -> mock used
        final String url = "http://" + domain + "/";

        final HtmlPage page = (HtmlPage) webClient.getPage(url);

        return page;
    }

    private void getByXPathLoop(final int count) throws Exception
    {
        final HtmlPage page = getHtmlPage();
        final String xpath = "id('content')/div[@class='floaty']/div[@class='block developers']/p/a[@class='devs']";

        for (int i = 0; i < count; i++)
        {
            // check the page
            final List<?> elements = page.getByXPath(xpath);
            Assert.assertEquals(1, elements.size());
        }
    }

    @Test
    @Ignore("Performance test")
    public void testGetByXPath() throws Exception
    {
        final Map<String, String[]> map = new HashMap<String, String[]>();

        map.put("http://" + domain + "/", new String[]
            {
                domain + "/home.html"
            });
        map.put("http://" + domain + "/home-files/mootools.js", new String[]
            {
                domain + "/home-files/mootools.js", "text/javascript"
            });
        map.put("http://" + domain + "/home-files/site.js", new String[]
            {
                domain + "/home-files/site.js", "text/javascript"
            });
        map.put("http://" + domain + "/home-files/ga.js", new String[]
            {
                domain + "/home-files/ga.js", "text/javascript"
            });
        map.put("http://" + domain + "/home-files/header.css", new String[]
            {
                domain + "/home-files/header.css", "text/css"
            });
        map.put("http://" + domain + "/home-files/style.css", new String[]
            {
                domain + "/home-files/style.css", "text/css"
            });

        map.put("http://" + domain + "/home-files/menubig_shadow.png", new String[]
            {
                domain + "/home-files/menubig_shadow.png"
            });
        map.put("http://" + domain + "/home-files/css/ads.png", new String[]
            {
                domain + "/home-files/css/ads.png"
            });
        map.put("http://" + domain + "/home-files/css/arrow.gif", new String[]
            {
                domain + "/home-files/css/arrow.gif"
            });
        map.put("http://" + domain + "/home-files/css/blog.gif", new String[]
            {
                domain + "/home-files/css/blog.gif"
            });
        map.put("http://" + domain + "/home-files/css/check.gif", new String[]
            {
                domain + "/home-files/css/check.gif"
            });
        map.put("http://" + domain + "/home-files/css/check.png", new String[]
            {
                domain + "/home-files/css/check.png"
            });
        map.put("http://" + domain + "/home-files/css/docs.gif", new String[]
            {
                domain + "/home-files/css/docs.gif"
            });
        map.put("http://" + domain + "/home-files/css/download.gif", new String[]
            {
                domain + "/home-files/css/download.gif"
            });
        map.put("http://" + domain + "/home-files/css/logo.gif", new String[]
            {
                domain + "/home-files/css/logo.gif"
            });
        map.put("http://" + domain + "/home-files/css/mediatemple.gif", new String[]
            {
                domain + "/home-files/css/mediatemple.gif"
            });
        map.put("http://" + domain + "/home-files/css/mucca.gif", new String[]
            {
                domain + "/home-files/css/mucca.gif"
            });
        map.put("http://" + domain + "/home-files/css/radio.gif", new String[]
            {
                domain + "/home-files/css/radio.gif"
            });
        map.put("http://" + domain + "/home-files/css/radio.png", new String[]
            {
                domain + "/home-files/css/radio.png"
            });
        map.put("http://" + domain + "/home-files/css/tab.gif", new String[]
            {
                domain + "/home-files/css/tab.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_big.gif", new String[]
            {
                domain + "/home-files/css/tab_big.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_docs.gif", new String[]
            {
                domain + "/home-files/css/tab_docs.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_download.gif", new String[]
            {
                domain + "/home-files/css/tab_download.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_forum.gif", new String[]
            {
                domain + "/home-files/css/tab_forum.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_small.gif", new String[]
            {
                domain + "/home-files/css/tab_small.gif"
            });
        map.put("http://" + domain + "/home-files/css/tab_trac.gif", new String[]
            {
                domain + "/home-files/css/tab_trac.gif"
            });
        map.put("http://" + domain + "/home-files/css/trac.gif", new String[]
            {
                domain + "/home-files/css/trac.gif"
            });

        webClient = getWebClient(domain, map, false, false);

        System.out.println("T===================================");

        // warm-up
        System.out.print("T= warmup...");
        getByXPathLoop(WARMUP_LOOPS);
        System.out.println("done");

        // test
        System.out.print("T= testGetByXPath: ");
        final long s = TimerUtils.getTime();
        getByXPathLoop(GETBYXPATH_LOOPS);
        final long r = TimerUtils.getTime() - s;
        System.out.println(r + " ms");
    }

    /**
     * Poor man's stand-alone test runner.
     */
    public static void main(final String[] args) throws Exception
    {
        final GetByXPathTest test = new GetByXPathTest();

        test.testGetByXPath();
    }
}
