package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.HandleConfirm;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case demonstrate how to handle confirm message boxes. We will run the action twice. The first time we will
 * push the OK button, the second time the Cancel button. To handle the confirm box we have to add a confirm handler.
 * Check the HandlerConfirm class for details.
 */
public class TConfirm extends AbstractTestCase
{
    /**
     * Demonstrating confirm box handling
     */
    @Test
    public void confirming() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the confirm page
        final GoToShowCase confirmPage = new GoToShowCase(homepage, "confirm");
        confirmPage.run();

        // push OK
        HandleConfirm handleConfirmPage = new HandleConfirm(confirmPage, true);
        handleConfirmPage.run();

        // push Cancel
        handleConfirmPage = new HandleConfirm(handleConfirmPage, false);
        handleConfirmPage.run();
    }
}
