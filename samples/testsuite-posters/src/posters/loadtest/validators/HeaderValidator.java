package posters.loadtest.validators;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.util.HtmlPageUtils;

/**
 * Checks for the correct header elements.
 */
public class HeaderValidator
{
    /**
     * Make a stateless singleton available.
     */
    private static final HeaderValidator instance = new HeaderValidator();

    /**
     * Checks the poster store header elements.
     * 
     * @param page
     *            the page to check
     */
    public void validate(final HtmlPage page) throws Exception
    {
        // assert presence of some basic elements in the header
        // the brand logo
        Assert.assertTrue("Brand not found.", HtmlPageUtils.isElementPresent(page, "//img[@class ='shopLogo']"));
        // The search form
        Assert.assertTrue("Search form not found.", HtmlPageUtils.isElementPresent(page, "id('header-search-trigger')"));
        // The search input
        Assert.assertTrue("Search input field not found.", HtmlPageUtils.isElementPresent(page, "id('s')"));
        // The search button
        Assert.assertTrue("Search button not found.", HtmlPageUtils.isElementPresent(page, "id('btnSearch')"));
        // The cart overview
        Assert.assertTrue("Cart overview in header not found.", HtmlPageUtils.isElementPresent(page, "id('headerCartOverview')"));
    }

    /**
     * The instance for easy reuse. Possible because this validator is stateless.
     * 
     * @return the instance
     */
    public static HeaderValidator getInstance()
    {
        return instance;
    }
}
