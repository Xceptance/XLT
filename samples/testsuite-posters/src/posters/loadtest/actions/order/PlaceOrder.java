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
 * Places the order.
 */
public class PlaceOrder extends AbstractHtmlPageAction
{
    /**
     * Place order button.
     */
    private HtmlElement placeOrderButton;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            The previously performed action
     */
    public PlaceOrder(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the place order button is available.
        Assert.assertTrue("Place order button not found.", HtmlPageUtils.isElementPresent(page, "id('btnOrder')"));

        // Remember the place order button.
        placeOrderButton = HtmlPageUtils.findSingleHtmlElementByID(page, "btnOrder");
    }

    @Override
    protected void execute() throws Exception
    {
        // Click the place order button.
        loadPageByClick(placeOrderButton);
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

        // Check that the order was successfully placed.
        final boolean successfulOrder = page.asXml().contains("Thank you for shopping with us!");
        Assert.assertTrue("Placing order failed.", successfulOrder);
    }
}
