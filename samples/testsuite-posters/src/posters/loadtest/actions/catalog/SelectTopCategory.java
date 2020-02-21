package posters.loadtest.actions.catalog;

import org.junit.Assert;

import posters.loadtest.validators.HeaderValidator;
import posters.loadtest.validators.NavBarValidator;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

/**
 * Selects a random top-category links and opens the related product overview page.
 */
public class SelectTopCategory extends AbstractHtmlPageAction
{
    /**
     * Chosen top-category.
     */
    private HtmlElement topCategoryLink;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            The previously performed action
     */
    public SelectTopCategory(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get all top category links and select one randomly.
        topCategoryLink = HtmlPageUtils.findHtmlElementsAndPickOne(getPreviousAction().getHtmlPage(),
                                                                   "id('categoryMenu')//a[@class='topCategoryMenuItem']");
    }

    @Override
    protected void execute() throws Exception
    {
        // Click the link.
        loadPageByClick(topCategoryLink);
    }

    @Override
    protected void postValidate() throws Exception
    {
        // Get the result of the action.
        final HtmlPage page = getHtmlPage();

        // First, we check all common criteria. This code can be bundled and
        // reused if needed. For the purpose of a programming example, we leave
        // it here as detailed as possible.

        // Check the response code, the singleton instance validates for 200.
        HttpResponseCodeValidator.getInstance().validate(page);

        // Check the content length, compare delivered content length to the
        // content length that was announced in the HTTP response header.
        ContentLengthValidator.getInstance().validate(page);

        // Check for complete HTML.
        HtmlEndTagValidator.getInstance().validate(page);

        // We can be pretty sure now, that the page fulfills the basic
        // requirements to be a valid page from our demo poster store.

        // Run more page specific tests now.
        // Check that we arrived on a category page.

        // Check for the header.
        HeaderValidator.getInstance().validate(page);

        // Check the side navigation.
        NavBarValidator.getInstance().validate(page);

        // The product over view element is present...
        Assert.assertTrue("Product over view element not present.", HtmlPageUtils.isElementPresent(page, "id('productOverview')"));

        // ...and we also see some poster's thumbnail images.
        HtmlPageUtils.findHtmlElements(page, "id('productOverview')/div/ul/li/div[@class='thumbnail']");

    }
}
