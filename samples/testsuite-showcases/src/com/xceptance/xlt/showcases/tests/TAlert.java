package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.ExecuteAlerts;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case shows the way XLT handles alert boxes. The alert boxes are only caught by an alert handler. It isn't
 * needed to push the OK button of the alert box (neither a way to do this). In the action we will first see a onclick
 * alert and afterwards a onload alert. For both cases we have to add an alert handler before.
 */
public class TAlert extends AbstractTestCase
{
    /**
     * Demonstrating alert box handling
     */
    @Test
    public void alerting() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the alert page
        final GoToShowCase alertPage = new GoToShowCase(homepage, "alert");
        alertPage.run();

        // now execute the alerts
        final ExecuteAlerts clickAlertPage = new ExecuteAlerts(alertPage);
        clickAlertPage.run();
    }
}
