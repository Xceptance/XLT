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
 * Selects a random sub-category links and opens the related product overview page.
 */
public class SelectCategory extends AbstractHtmlPageAction
{
    /**
     * Chosen level-1 category.
     */
    private HtmlElement categoryLink;

    /**
     * Constructor
     * 
     * @param previousAction
     *            The previously performed action
     */
    public SelectCategory(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get all drop down item links and select one randomly.
        categoryLink = HtmlPageUtils.findHtmlElementsAndPickOne(getPreviousAction().getHtmlPage(),
                                                                "id('categoryMenu')//ul[@class='dropdown-menu']/li/a");

    }

    @Override
    protected void execute() throws Exception
    {
        // Click the link.
        loadPageByClick(categoryLink);

    }

    @Override
    protected void postValidate() throws Exception
    {
        // Get the result of the action.
        final HtmlPage page = getHtmlPage();

        // Basic checks that are part of the XLT API.
        HttpResponseCodeValidator.getInstance().validate(page);
        ContentLengthValidator.getInstance().validate(page);
        HtmlEndTagValidator.getInstance().validate(page);

        // Check for the header.
        HeaderValidator.getInstance().validate(page);

        // Check the side navigation.
        NavBarValidator.getInstance().validate(page);

        // The product over view element is present....
        Assert.assertTrue("Product over view element is bot present", HtmlPageUtils.isElementPresent(page, "id('productOverview')"));

        // ...and we also see some poster's thumbnail images.
        HtmlPageUtils.findHtmlElements(page, "id('productOverview')/div/ul/li/div[@class='thumbnail']");

    }
}
