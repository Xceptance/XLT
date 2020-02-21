package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.ExecuteAjax;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case will demonstrate how we can handle ajax requests. On our show case page the content of a div is
 * changed through a button click that executes an ajax request. We will check the content of the div before and
 * afterwards.
 */
public class TAjax extends AbstractTestCase
{
    /**
     * Demonstrating ajax handling
     */
    @Test
    public void usingAjax() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the ajax page
        final GoToShowCase ajaxPage = new GoToShowCase(homepage, "ajax");
        ajaxPage.run();

        // execute ajax
        final ExecuteAjax executeAjaxPage = new ExecuteAjax(ajaxPage);
        executeAjaxPage.run();
    }
}
