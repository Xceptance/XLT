package posters.loadtest.tests;

import org.junit.Test;

import posters.loadtest.actions.Homepage;
import posters.loadtest.flows.BrowsingFlow;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Open the homepage, browse the catalog. If there's a product overview open a random posters detail view.
 **/
public class TBrowse extends AbstractTestCase
{
    /**
     * Main test method
     */
    @Test
    public void browsePosterStore() throws Throwable
    {
        // Read the store URL from properties.
        final String url = getProperty("store-url", "http://localhost:8080/posters/");

        // The probability to perform a paging during browsing the categories
        final int pagingProbability = getProperty("paging.probability", 0);

        // The min. number of paging rounds
        final int pagingMin = getProperty("paging.min", 0);

        // The max. number of paging rounds
        final int pagingMax = getProperty("paging.max", 0);

        // The probability to select a top category during browsing
        final int topCategoryProbability = getProperty("browsing.topCategoryProbability", 0);

        // The min. number of products to browse, search and add to cart
        final int productsMin = getProperty("products.min", 0);

        // The max. number of products to browse, search and add to cart
        final int productsMax = getProperty("products.max", 0);

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();

        // select randomly the number of products to browse
        final int numberOfProducts = XltRandom.nextInt(productsMin, productsMax);
        for (int round = 0; round < numberOfProducts; round++)
        {
            // Browse the catalog and view a product detail page.
            // The browsing is encapsulated in a flow that combines a sequence of several XLT actions.
            // Different test cases can call this method now to reuse the flow.
            // This is a concept for code structuring you can implement if needed, yet explicit support
            // is neither available in the XLT framework nor necessary when you manually create a flow.
            final BrowsingFlow browsingFlow = new BrowsingFlow(homepage, topCategoryProbability, pagingProbability, pagingMin,
                                                               pagingMax);
            browsingFlow.run();
        }
    }
}
