package posters.loadtest.actions.order;

import org.junit.Assert;

import posters.loadtest.validators.HeaderValidator;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

/**
 * Opens the cart overview page.
 */
public class ViewCart extends AbstractHtmlPageAction
{
    /**
     * Link to shopping cart page.
     */
    private HtmlElement viewCartLink;

    /**
     * Constructor
     * 
     * @param previousAction
     *            The previously performed action
     */
    public ViewCart(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the cart overview link is available.
        Assert.assertTrue("Cart overview link not found", HtmlPageUtils.isElementPresent(page, "id('miniCartMenu')//div[@class='linkButton']/a"));

        // Remember cart overview link.
        viewCartLink = HtmlPageUtils.findSingleHtmlElementByXPath(page, "id('miniCartMenu')//div[@class='linkButton']/a");
    }

    @Override
    protected void execute() throws Exception
    {
        // Load the cart overview page.
        loadPageByClick(viewCartLink);
    }

    @Override
    protected void postValidate() throws Exception
    {
        // Get the result of the action.
        final HtmlPage page = getHtmlPage();

        // Basic checks - see action 'Homepage' for some more details how and when to use these validators.
        HttpResponseCodeValidator.getInstance().validate(page);
        ContentLengthValidator.getInstance().validate(page);
        HtmlEndTagValidator.getInstance().validate(page);

        HeaderValidator.getInstance().validate(page);

        // Check that it's the cart overview page.
        Assert.assertTrue("Title not found", HtmlPageUtils.isElementPresent(page, "id('titleCart')"));
        Assert.assertTrue("Total price not found", HtmlPageUtils.isElementPresent(page, "id('orderSubTotalValue')"));
        Assert.assertTrue("Checkout button not found", HtmlPageUtils.isElementPresent(page, "id('btnStartCheckout')"));
    }
}
