package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.actions.PromptString;

/**
 * This test case demonstrate the handling of prompt boxes. Our page contains a button which opens a prompt box. The
 * entered message is displayed on the page. To handle the prompt box we have to add a PromptHandler. Consider that some
 * pattern could be refused by the prompt, e.g. <script>
 */
public class TPrompt extends AbstractTestCase
{
    /**
     * Demonstrating prompt box handling
     */
    @Test
    public void prompting() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the prompt page
        final GoToShowCase promptPage = new GoToShowCase(homepage, "prompt");
        promptPage.run();

        // enter specified string
        final PromptString handlePromptPage = new PromptString(promptPage, "test");
        handlePromptPage.run();
    }
}
