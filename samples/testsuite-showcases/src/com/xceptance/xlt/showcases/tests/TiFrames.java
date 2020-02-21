package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.IFramesHandling;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case demonstrate the handling of iframes. It is quite similar to the handling of frames. So feel free to
 * check the TFrames tes tcase, too. In this test case you can see that there is no problem to handle
 * "frames in frames". So the demonstrate page contains an iframe with a link which displays the page in the iframe
 * again.
 */
public class TiFrames extends AbstractTestCase
{
    /**
     * Demonstrating iframe handling
     */
    @Test
    public void iframing() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the iframe page
        final GoToShowCase iframePage = new GoToShowCase(homepage, "iframes");
        iframePage.run();

        // click the first link
        IFramesHandling ifameHandlingPage = new IFramesHandling(iframePage);
        ifameHandlingPage.run();

        // a loop to click the link several times
        for (int i = 1; i < 10; i++)
        {
            ifameHandlingPage = new IFramesHandling(ifameHandlingPage);
            ifameHandlingPage.run();
        }
    }
}
