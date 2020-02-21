package posters.loadtest.tests;

import org.junit.Test;

import posters.loadtest.actions.Homepage;

import com.xceptance.xlt.api.tests.AbstractTestCase;

/**
 * This test case simulates a single click visit. The visitor opens the poster store landing page and will not do any
 * interaction.
 */
public class TVisit extends AbstractTestCase
{
    /**
     * Main test method
     */
    @Test
    public void visitPosterStore() throws Throwable
    {
        // Read the store URL from properties.
        final String url = getProperty("store-url", "http://localhost:8080/posters/");

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();
    }
}
