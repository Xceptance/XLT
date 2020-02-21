package com.xceptance.xlt.showcases.tests;

import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.flow.CrawlerFlow;
import com.xceptance.xlt.showcases.flow.CrawlerFlowConfig;
import com.xceptance.xlt.showcases.flow.XSSCheckFlowConfig;
import com.xceptance.xlt.showcases.util.PropertyUtils;

/**
 * Compare with TCrawler test case. But this test case use the possibility to check all forms for xss vulnerabilities.
 * You can specify the attack strings and the runtime of the xss check. During the xss check each input is tested
 * against the provided attack string. The check is done first separate for each input of a form and afterwards together
 * for the whole form. On the resulting page we check if we can find the attack string without encoding, because this is
 * a hint for a xss vulnerability. Limitations of this test are required input formats and forms on restricted pages. As
 * solution for the second problem it is possible to integrate the XSSCheckFlow into your test case. Compare with
 * XSSCheckFlow.
 */
@Ignore
public class TXSSCrawler extends AbstractTestCase
{
    /**
     * Test pages for XSS vulnerabilities.
     */

    @Test
    public void xssAttack() throws Throwable
    {
        // read the start from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // create XSS check configuration and set values
        final XSSCheckFlowConfig xssCheckConfig = new XSSCheckFlowConfig();
        xssCheckConfig.setRuntime(getProperty("xssCheckRuntime", 15));
        xssCheckConfig.setXssAttackStrings(PropertyUtils.propertyToList(getProperty("attackStrings")));

        // create crawler config and set values
        final CrawlerFlowConfig crawlerConfig = new CrawlerFlowConfig();
        crawlerConfig.setXssCheckConfig(xssCheckConfig);
        crawlerConfig.setDepthOfRecursion(getProperty("depthOfRecursion", 2));
        crawlerConfig.setCheckXSS(true);
        crawlerConfig.setExcludePatterns(PropertyUtils.propertyToList(getProperty("excludePatterns")));
        crawlerConfig.setIncludePatterns(PropertyUtils.propertyToList(getProperty("includePatterns")));
        crawlerConfig.setProceedExternals(getProperty("proceedExternals", false));
        crawlerConfig.setRuntime(getProperty("crawlerRuntime", 5));
        crawlerConfig.setUrlIndicators(getProperty("urlIndicators", "").split("\\|"));

        // open start page
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the crawler page
        final GoToShowCase crawler = new GoToShowCase(homepage, "crawler");
        crawler.run();

        // start crawler flow
        final CrawlerFlow crawlerFlow = new CrawlerFlow();
        crawlerFlow.run(crawler, crawlerConfig);
    }
}
