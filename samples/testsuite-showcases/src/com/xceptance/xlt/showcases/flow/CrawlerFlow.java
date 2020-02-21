package com.xceptance.xlt.showcases.flow;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * Crawler: This is the flow class which could be used in established test cases. This class use the provided
 * page(current) and checks all links on the page and depending on the provided configuration child pages or external
 * pages. It is also possible to call the xss check through enabling the xssCheck setting. The crawler creates is own
 * branch of the flow and perform the configured actions. Afterwards the branch is dropped and the regular flow
 * continues.
 */
public class CrawlerFlow
{

    /**
     * Check links depending on recursion depth and it is possible to enable XSS checks on each page
     * 
     * @param previousAction
     *            The previous action.
     * @param crawlerConfig
     *            Configuration for the crawler
     * @throws Throwable
     */
    public void run(final AbstractHtmlPageAction previousAction, final CrawlerFlowConfig crawlerConfig) throws Throwable
    {

        // Contains the status of the duplicate indicators(patterns for quite
        // similar pages e.g. Product pages in a catalog)
        // at the first occurrence of a indicator, this indicator is added to
        // this list. So we will know that we found this indicator at later
        // occurrences
        final List<String> processedIndicators = new LinkedList<String>();

        // list for all urls (visited and going to visit)
        final List<String> urls = new LinkedList<String>();

        // list for recursion depths of corresponding url
        final List<Integer> recursionDepths = new LinkedList<Integer>();

        // list for referrers of corresponding urls )
        final List<String> referrers = new LinkedList<String>();

        // variable for the loop
        int processedURLs = 0;

        // this is the page
        AbstractHtmlPageAction openCandidate = previousAction;

        // add information of start page to url list
        urls.add(openCandidate.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString());
        recursionDepths.add(0);
        referrers.add("");

        // add urls from include patterns
        for (final String include : crawlerConfig.getIncludePatterns())
        {
            final String absoluteUrl = UrlUtils.resolveUrl(openCandidate.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString(),
                                                           include);
            Assert.assertNotNull(absoluteUrl);
            urls.add(absoluteUrl);
            recursionDepths.add(0);
            referrers.add("");
        }

        // set timeout
        final long timeout = System.currentTimeMillis() + (crawlerConfig.getRuntime() * 60000);

        // run until timeout
        while (timeout > System.currentTimeMillis())
        {

            // check if we have to do xss checks
            if (crawlerConfig.isCheckXSS())
            {
                // run xss checks
                final XSSCheckFlow xssCheckFlow = new XSSCheckFlow();
                xssCheckFlow.run(openCandidate, crawlerConfig.getXssCheckConfig());
            }

            // check if we reach the max recursion depth
            // if not start url add process
            if (crawlerConfig.getDepthOfRecursion() > recursionDepths.get(processedURLs))
            {
                // collect all anchors
                final List<HtmlAnchor> anchors = openCandidate.getHtmlPage().getAnchors();

                // feed the url list with the available anchors
                for (final HtmlAnchor anchor : anchors)
                {
                    // ignore mailto links, local anchors, pdfs, pngs, ..
                    // this could also be defined as exclude Pattern in
                    // project.properties
                    if (anchor.getHrefAttribute().startsWith("mailto:") || anchor.getHrefAttribute().contains("#") ||
                        anchor.getHrefAttribute().endsWith(".pdf") || anchor.getHrefAttribute().endsWith(".png") ||
                        anchor.getHrefAttribute().endsWith(".jpg") || anchor.getHrefAttribute().endsWith(".txt") ||
                        anchor.getHrefAttribute().contains(".xml"))
                    {
                        continue;
                    }

                    // check the exclude pattern
                    boolean excludeSkip = false;

                    for (final String exclude : crawlerConfig.getExcludePatterns())
                    {
                        // if anchor contains exclude pattern skip
                        if (anchor.getHrefAttribute().contains(exclude))
                        {
                            excludeSkip = true;
                            break;
                        }
                    }
                    if (excludeSkip)
                    {
                        continue;
                    }

                    // ensure that we have an absolute url
                    final String absoluteUrl = UrlUtils.resolveUrl(openCandidate.getHtmlPage().getWebResponse().getWebRequest().getUrl()
                                                                                .toString(), anchor.getHrefAttribute());
                    Assert.assertNotNull(absoluteUrl);

                    // check if we have an link to an external server and if we
                    // should skip external links
                    if (!absoluteUrl.contains(openCandidate.getHtmlPage().getWebResponse().getWebRequest().getUrl().getHost()) &&
                        !crawlerConfig.isProceedExternals())
                    {
                        continue;
                    }

                    // check if the anchor contains processed indicator
                    boolean processedIndicator = false;

                    // loop over the indicators
                    for (int i = 0; i < crawlerConfig.getUrlIndicators().length && !processedIndicator &&
                                    !crawlerConfig.getUrlIndicators()[i].equals(""); i++)
                    {
                        final String indicator = crawlerConfig.getUrlIndicators()[i];

                        // check if the url contains the duplicate indicator
                        // if not continue the check with the next indicator
                        if (!absoluteUrl.contains(indicator))
                        {
                            continue;
                        }
                        else
                        {
                            // check if this is the first time we found this
                            // indicator
                            if (!processedIndicators.contains(indicator))
                            {
                                // if not we add the indicator to the list for
                                // later occurrences and continue the general
                                // flow
                                processedIndicators.add(indicator);
                            }
                            else
                            {
                                // if the list contains the indicator
                                // set status to true. So we will skip
                                // proceeding
                                processedIndicator = true;
                            }
                        }
                    }

                    // in case we already processed url indicator continue with
                    // next anchor
                    if (processedIndicator)
                    {
                        continue;
                    }

                    // add url only when it isn't in the list already
                    if (!urls.contains(absoluteUrl))
                    {
                        // create a random index (between actual index and list
                        // size)
                        final int random = XltRandom.nextInt(processedURLs + 1, urls.size());
                        urls.add(random, absoluteUrl);
                        recursionDepths.add(random, recursionDepths.get(processedURLs) + 1);
                        referrers.add(random, urls.get(processedURLs));
                    }
                }
            }

            // now the loop variable is increases so we get the next url (if
            // available)
            processedURLs++;

            // check if we have a unvisited url
            if (processedURLs < urls.size())
            {

                // go to next page
                openCandidate = new OpenPage(openCandidate, urls.get(processedURLs), referrers.get(processedURLs),
                                             crawlerConfig.getRequiredText(), crawlerConfig.getDisallowedText());
                openCandidate.run();
            }
            else
            {
                break;
            }
        }
    }
}
