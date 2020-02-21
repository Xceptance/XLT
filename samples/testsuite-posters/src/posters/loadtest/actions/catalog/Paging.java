package posters.loadtest.actions.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import posters.loadtest.util.AjaxUtils;
import posters.loadtest.validators.HeaderValidator;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

/**
 * Performs a paging.<br/>
 * This action does not result in a page load but consists of a sequence of AJAX calls. JavaScript is disabled due to
 * performance reasons. So assembling the request parameters, make the call and evaluating the response content makes
 * this kind of actions a bit more complex.
 */
public class Paging extends AbstractHtmlPageAction
{
    /**
     * Page number we start from.
     */
    private int currentPageNumber;

    /**
     * Page number we go to.
     */
    private int targetPageNumber;

    /**
     * The path of the current page url. This is needed to determine the current page type. Possibilities are a top
     * category overview page, a sub category overview page or a search results overview page.
     */
    private String path;

    /**
     * The ID of the current category.
     */
    private String categoryId;

    /**
     * The JavaScript code for the paging functionality
     */
    private String scriptCodeAsString;

    /**
     * The URL for the getProductOfTopCategory AJAX call
     */
    private String getProductOfTopCategoryURL;

    /**
     * The URL for the getProductOfSubCategory AJAX call
     */
    private String getProductOfSubCategoryURL;

    /**
     * The URL for the getProductOfSearch AJAX call
     */
    private String getProductOfSearchURL;

    /**
     * Constructor
     * 
     * @param previousAction
     *            The previously performed action
     */
    public Paging(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the current page.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);

        // Check the current page is a product overview page.
        Assert.assertTrue("No product overview page, so paging is impossible.",
                          HtmlPageUtils.isElementPresent(page, "id('productOverview')"));

        // Get the current page number.
        currentPageNumber = Integer.parseInt(HtmlPageUtils.findSingleHtmlElementByID(page, "productOverview").getAttribute("currentPage"));
        // The paging is build with JavaScript, so we need to extract the information out of the responsible JavaScript
        // code.
        final HtmlElement scriptElement = HtmlPageUtils.findSingleHtmlElementByXPath(page,
                                                                                     "id('main')/div/div/div[@id='pagination']/following-sibling::script");
        scriptCodeAsString = scriptElement.getTextContent();

        final int beginIndex = scriptCodeAsString.indexOf("totalPages: ") + 12;
        final int totalPageCount = Integer.parseInt(scriptCodeAsString.substring(beginIndex, scriptCodeAsString.indexOf(",", beginIndex)));
        targetPageNumber = XltRandom.nextInt(1, totalPageCount);

        // Be sure, that the target page number is not the current page number.
        Assert.assertFalse("The total page count is 1. Paging is not possible.", totalPageCount == 1);

        if (totalPageCount > 1)
        {
            while (currentPageNumber == targetPageNumber)
            {
                targetPageNumber = XltRandom.nextInt(1, totalPageCount);
            }
        }

        // Get the path of the current URL to later extract the page type.
        path = page.getUrl().getPath();

        categoryId = page.getUrl().getQuery().substring(11);

        // The JavaScript code also contains the URLs that we need to perform the subsequent AJAX call to get the
        // products of the next page.
        // Depending on the current page type we need one of the three extracted URLs
        final List<String> URLStrings = RegExUtils.getAllMatches(scriptCodeAsString, "\\$\\.post\\('([^']*)", 1);
        getProductOfTopCategoryURL = URLStrings.get(0);
        getProductOfSubCategoryURL = URLStrings.get(1);
        getProductOfSearchURL = URLStrings.get(2);

    }

    @Override
    protected void execute() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // Build the request parameters of the AJAX call.
        final List<NameValuePair> pagingParams = new ArrayList<NameValuePair>();
        pagingParams.add(new NameValuePair("page", Integer.toString(targetPageNumber)));

        // Variable that holds the response of the AJAX call.
        WebResponse response = null;

        // Check if current page is a top category overview page.
        if (path.contains("topCategory"))
        {
            // Add categoryId to request parameters.
            pagingParams.add(new NameValuePair("categoryId", categoryId));
            // Execute the AJAX call and get the response.
            response = AjaxUtils.callPost(page, getProductOfTopCategoryURL, pagingParams);
        }
        // Check if current page is a sub category overview page.
        else if (path.contains("category"))
        {
            // Add categoryId to request parameters.
            pagingParams.add(new NameValuePair("categoryId", categoryId));
            // Execute the AJAX call and get the response.
            response = AjaxUtils.callPost(page, getProductOfSubCategoryURL, pagingParams);
        }
        // Check if current page shows some search results.
        else if (path.contains("search"))
        {
            pagingParams.add(new NameValuePair("searchText", HtmlPageUtils.findSingleHtmlElementByID(page, "searchText").asText()));
            // Execute the AJAX call and get the response.
            response = AjaxUtils.callPost(page, getProductOfSearchURL, pagingParams);
        }
        // Encountered unknown page type.
        else
        {
            Assert.fail("Unknown page type.");
        }

        // Update the page and show new products.

        // (1) Remove current products from page.
        final HtmlElement productOverview = HtmlPageUtils.findSingleHtmlElementByID(page, "productOverview");
        productOverview.removeAllChildren();

        // Get JSON object from response.
        final JSONObject jsonResponse = new JSONObject(response.getContentAsString());

        // Get all products from JSON object.
        final JSONArray products = (JSONArray) jsonResponse.get("products");

        // Remember the current index of the list.
        HtmlElement ulElement = null;

        // (2) Render each product:
        for (int i = 0; i < products.length(); i++)
        {
            final JSONObject product = (JSONObject) products.get(i);
            // (2.1) Create a new row of products in the product grid.
            if (i % 3 == 0)
            {
                ulElement = HtmlPageUtils.createHtmlElement("ul", HtmlPageUtils.createHtmlElement("div", productOverview));
                ulElement.setAttribute("class", "thumbnails");
            }
            // (2.2) Add product as a list item.
            final HtmlElement productTag = HtmlPageUtils.createHtmlElement("div", HtmlPageUtils.createHtmlElement("li", ulElement));
            productTag.setId("product" + i);
            productTag.setAttribute("class", "thumbnail");

            // (2.3) Create link to product detail page.
            final HtmlElement productLink = HtmlPageUtils.createHtmlElement("a", productTag);
            final HtmlElement contextPathScriptElement = HtmlPageUtils.findSingleHtmlElementByXPath(page,
                                                                                                    "html/head/script[@type='text/javascript' and contains(.,'CONTEXT_PATH')]");
            final String contextPath = RegExUtils.getFirstMatch(contextPathScriptElement.getTextContent(), "CONTEXT_PATH = '(.*)';", 1);
            productLink.setAttribute("href", contextPath + "/productDetail/" + URLEncoder.encode(product.get("name").toString(), "UTF-8") +
                                             "?productId=" + product.get("id"));
            // (2.4) Add image tag.
            final HtmlElement imageTag = HtmlPageUtils.createHtmlElement("img", productLink);
            imageTag.setAttribute("src", contextPath + product.get("imageURL"));
        }

        // (3) Set the current page number.
        // Although not visually rendered (i.e. in the XLT Result Browser)
        // it is important for potential subsequent paging actions.
        HtmlPageUtils.findSingleHtmlElementByID(page, "productOverview").setAttribute("currentPage", Integer.toString(targetPageNumber));

        // (4) Publish the results.
        setHtmlPage(page);
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

        // Check the current page is a product overview page...
        Assert.assertTrue("Product Overview element missing.", HtmlPageUtils.isElementPresent(page, "id('productOverview')"));

        // ...and we also see some poster's thumbnail images.
        HtmlPageUtils.findHtmlElements(page, "id('productOverview')/div/ul/li/div[@class='thumbnail']");
    }
}
