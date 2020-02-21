package posters.loadtest.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import posters.loadtest.actions.Homepage;
import posters.loadtest.actions.Search;
import posters.loadtest.actions.catalog.Paging;
import posters.loadtest.actions.catalog.ProductDetailView;
import posters.loadtest.util.SearchOption;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.data.DataProvider;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Open the landing page and search for predefined key words as well as for random strings. If there are search results
 * open a random product's detail view.
 */
public class TSearch extends AbstractTestCase
{
    /**
     * Data provider for search phrases that result in hits..
     */
    private static final DataProvider HITS_PROVIDER;

    static
    {
        try
        {
            // Initialize the search provider with file containing the search phrases
            HITS_PROVIDER = DataProvider.getInstance(DataProvider.DEFAULT + File.separator + "search_phrases.txt");
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Main test method.
     * 
     * @throws Throwable
     */
    @Test
    public void search() throws Throwable
    {
        // The previous action
        AbstractHtmlPageAction previousAction;

        // Read the store URL from properties.
        final String url = getProperty("store-url", "http://localhost:8080/posters/");

        // The probability to perform a paging during browsing the categories
        final int pagingProbability = getProperty("paging.probability", 0);

        // The min. number of paging rounds
        final int pagingMin = getProperty("paging.min", 0);

        // The max. number of paging rounds
        final int pagingMax = getProperty("paging.max", 0);

        // The probability to perform a search without hits
        final int searchNoHitsProbability = getProperty("search.nohits.probability", 0);

        // The minimum number of products to search
        final int productsMin = getProperty("products.min", 1);

        // The maximum number of products to search
        final int productsMax = getProperty("products.max", 1);

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();
        previousAction = homepage;

        // Get the number of searches determined from the configured min and max
        // products.
        final int searches = XltRandom.nextInt(productsMin, productsMax);
        for (int searchRound = 0; searchRound < searches; searchRound++)
        {
            // The search option is the indicator whether to search for one of
            // the search phrases from the 'HITS_PROVIDER' that results in a hit
            // or a generated phrase that results in a 'no results' page.
            final SearchOption option = getSearchOption(searchNoHitsProbability);

            // Run the search with an appropriate search phrase according to the
            // search option.
            final Search search = new Search(previousAction, getSearchPhrase(option), option);
            search.run();
            previousAction = search;

            // Perform Paging and go to product detail page of a result according to the search option
            if (option == SearchOption.HITS)
            {
                // According to the configured probability perform the paging or not.
                if (XltRandom.nextBoolean(pagingProbability))
                {
                    // Get current number of paging rounds determined from the configured
                    // min and max value for paging.
                    final int pagingRounds = XltRandom.nextInt(pagingMin, pagingMax);
                    for (int paginRound = 0; paginRound < pagingRounds; paginRound++)
                    {
                        // perform a paging if possible
                        final Paging paging = new Paging(previousAction);
                        if (paging.preValidateSafe())
                        {
                            paging.run();
                        }
                        else
                        {
                            break;
                        }
                        previousAction = paging;
                    }
                }
                // product detail view
                final ProductDetailView productDetailView = new ProductDetailView(previousAction);
                productDetailView.run();
                previousAction = productDetailView;
            }
        }
    }

    /**
     * Returns a search option using the given probability.
     * 
     * @param searchNoHitsProbability
     *            probability to grab the {@link SearchOption#NO_HITS} search option
     * @return search option
     */
    private SearchOption getSearchOption(final int searchNoHitsProbability)
    {
        if (XltRandom.nextBoolean(searchNoHitsProbability))
        {
            return SearchOption.NO_HITS;
        }
        else
        {
            return SearchOption.HITS;
        }
    }

    /**
     * Returns a search phrase.
     * 
     * @return search phrase
     */
    protected String getSearchPhrase(final SearchOption option)
    {
        switch (option)
        {
            case HITS:
                // Return one of the predefined search phrases.
                return HITS_PROVIDER.getRandomRow(false);

            case NO_HITS:
                // Return a random alphanumeric string, make it random and long enough.
                return RandomStringUtils.randomAlphabetic(XltRandom.nextInt(5, 10)) + " " +
                       RandomStringUtils.randomAlphabetic(XltRandom.nextInt(8, 10));
            default:
                return null;
        }
    }
}
