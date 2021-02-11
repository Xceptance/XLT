/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Opens the product detail page for a randomly chosen poster.
 */
public class ProductDetailView extends AbstractHtmlPageAction
{
    /**
     * The product detail link to follow
     */
    private HtmlElement productDetailLink;

    /**
     * Constructor
     * 
     * @param previousAction
     *            The previously performed action
     */
    public ProductDetailView(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the current page.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);

        // Check the current page is a product overview category page...
        Assert.assertTrue("Product Overview element missing.", HtmlPageUtils.isElementPresent(page, "id('productOverview')"));

        // ..and we also see some poster's thumbnail images.
        HtmlPageUtils.findHtmlElements(page, "id('productOverview')/div/ul/li/div[@class='thumbnail']");

        // Remember a random product's link URL.
        productDetailLink = HtmlPageUtils.findHtmlElementsAndPickOne(page, "id('productOverview')//div[@class='thumbnail']/div/a");
        Assert.assertNotNull("No matching product detail link found.", productDetailLink);

    }

    @Override
    protected void execute() throws Exception
    {
        // Click on the chosen product detail link to load the product detail page.
        loadPageByClick(productDetailLink);

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
        NavBarValidator.getInstance().validate(page);

        // Check it's a product detail page.
        // The product's name element in the headline is present.
        HtmlPageUtils.isElementPresent(page, "id('main')/div/div/h1[@id='titleProductName']");

        // The product description is there in the right presentation (h3 - h4 -span).
        Assert.assertTrue("Product description is not there or not in the right presentation (h3 - h4 -span).",
                          HtmlPageUtils.isElementPresent(page, "id('prodDescriptionOverview')"));
        Assert.assertTrue("Product description is not there or not in the right presentation (h3 - h4 -span).",
                          HtmlPageUtils.isElementPresent(page, "id('prodDescriptionDetail')"));
        // There is a price with the correct currency.
        final HtmlElement productPriceElement = HtmlPageUtils.findSingleHtmlElementByID(page, "prodPrice");
        final String productPrice = productPriceElement.getTextContent();
        Assert.assertTrue("The price does not start with $", productPrice.startsWith("$"));

        // Product configuration elements are present.
        Assert.assertTrue("Product configuration element (finish matte) is not present.",
                          HtmlPageUtils.isElementPresent(page, "id('finish-matte')"));
        Assert.assertTrue("Product configuration element (finish gloss) is not present.",
                          HtmlPageUtils.isElementPresent(page, "id('finish-gloss')"));
        Assert.assertTrue("Product configuration element (size) is not present.", 
                          HtmlPageUtils.isElementPresent(page, "id('selectSize')"));

        // 'Add to cart' button is available.
        Assert.assertTrue("'Add to cart' button is not available", HtmlPageUtils.isElementPresent(page, "id('btnAddToCart')"));

    }
}
