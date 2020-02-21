package com.xceptance.xlt.showcases.tests;

import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.flow.CrawlerFlow;
import com.xceptance.xlt.showcases.flow.CrawlerFlowConfig;
import com.xceptance.xlt.showcases.util.PropertyUtils;

/**
 * This test will crawl a provided page. This test case will check all links on this page and child pages(excluding
 * duplicate links) until a configured criteria is true. It is possible to set the runtime, the recursion depth or if
 * the crawler should check external links. With include and exclude patterns we can configure an individual
 * handling(visit or not) for some pages. With urlIndicators you can specify patterns of URLs which should only be
 * visited one time(e.g. for nearly similar pages) It is also possible to specify text patterns which are required or
 * forbidden on each page.
 */
@Ignore
public class TCrawler extends AbstractTestCase
{
    /**
     * crawl a site.
     */

    @Test
    public void crawl() throws Throwable
    {
        // read the start from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // create crawler configuration and set values
        final CrawlerFlowConfig crawlerConfig = new CrawlerFlowConfig();
        crawlerConfig.setDepthOfRecursion(getProperty("depthOfRecursion", 10));
        crawlerConfig.setCheckXSS(false);
        crawlerConfig.setExcludePatterns(PropertyUtils.propertyToList(getProperty("excludePatterns")));
        crawlerConfig.setIncludePatterns(PropertyUtils.propertyToList(getProperty("includePatterns")));
        crawlerConfig.setProceedExternals(getProperty("proceedExternals", false));
        crawlerConfig.setRuntime(getProperty("crawlerRuntime", 5));
        crawlerConfig.setUrlIndicators(getProperty("urlIndicators", "").split("\\|"));
        crawlerConfig.setRequiredText(PropertyUtils.propertyToList(getProperty("requiredText")));

        // open start page
        final OpenPage startPage = new OpenPage(startUrl);
        startPage.run();

        // start crawler flow
        final CrawlerFlow crawlerFlow = new CrawlerFlow();
        crawlerFlow.run(startPage, crawlerConfig);
    }
}
