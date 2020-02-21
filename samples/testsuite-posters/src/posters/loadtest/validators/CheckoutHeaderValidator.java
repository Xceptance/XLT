package posters.loadtest.validators;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.util.HtmlPageUtils;

/**
 * Checks for the correct header elements.
 */
public class CheckoutHeaderValidator
{
    /**
     * Make a stateless singleton available.
     */
    private static final CheckoutHeaderValidator instance = new CheckoutHeaderValidator();

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
        Assert.assertTrue("Brand not found.", HtmlPageUtils.isElementPresent(page, "id('brand')"));
        // the showUserMenu button
        Assert.assertTrue("Cart overview in header not found.", HtmlPageUtils.isElementPresent(page, "id('showUserMenu')"));
    }

    /**
     * The instance for easy reuse. Possible because this validator is stateless.
     * 
     * @return the instance
     */
    public static CheckoutHeaderValidator getInstance()
    {
        return instance;
    }
}
