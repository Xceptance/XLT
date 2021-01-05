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
package posters.loadtest.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;

import posters.loadtest.util.AjaxUtils;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;

/**
 * Adds a previously configured product to cart.<br/>
 * This action does not result in a page load but consists of a sequence of three AJAX calls. JavaScript is disabled due
 * to performance reasons. So assembling the request parameters, make the call and evaluating the response content makes
 * this kind of actions a bit more complex.
 */
public class AddToCart extends AbstractHtmlPageAction
{
    /**
     * The ID of the product to add to cart.
     */
    private String productId;

    /**
     * The selected poster size.
     */
    private String size;

    /**
     * The selected poster finish (matte or gloss).
     */
    private String finish;

    /**
     * The 'Add to cart' form.
     */
    private HtmlForm addToCartForm;

    /**
     * Constructor.
     *
     * @param previousAction
     *            The previous action.
     */
    public AddToCart(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Look up the 'add to cart' form.
        addToCartForm = HtmlPageUtils.findSingleHtmlElementByID(page, "addToCartForm");

        // Configure the product by selecting a random finish (matte or gloss).
        finish = HtmlPageUtils.findHtmlElementsAndPickOne(addToCartForm, "id('selectStyle')/div/label").getTextContent().trim();

        // We choose one of the size options randomly and remember it as a string.
        // We will need it as a parameter later on in the subsequent AJAX calls
        // to update the price and add poster to cart.
        final HtmlElement option = HtmlPageUtils.findHtmlElementsAndPickOne(page, "id('selectSize')/option");
        // Get the text content of the element as trimmed string.
        size = option.getTextContent().trim();

        // Get the product ID. This is also needed for the AJAX calls.
        productId = HtmlPageUtils.findSingleHtmlElementByXPath(page, "id('addToCartForm')/div[@class='row']")
                                 .getAttribute("id");

        // Assert the presence of the add to cart button (even though we do not use
        // it here since we're just simulating the AJAX calls that are normally
        // triggered by JavaScript after hitting the button).
        Assert.assertTrue("AddToCart button is not present.", HtmlPageUtils.isElementPresent(page, "id('btnAddToCart')"));

    }

    @Override
    protected void execute() throws Exception
    {
        // Get the result of the this action.
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // (1) Update price.
        // First we collect the (POST) request parameters for the call and
        // create a list of name value pairs, one for each parameter.
        final List<NameValuePair> updatePriceParams = new ArrayList<NameValuePair>();
        updatePriceParams.add(new NameValuePair("productId", productId));
        updatePriceParams.add(new NameValuePair("size", size));

        // Perform the AJAX call and return the result.
        final WebResponse updatePriceResponse = AjaxUtils.callPost(page, "/posters/updatePrice", updatePriceParams);

        
        // Get JSON object from response.
        final JSONObject updatePriceJsonResponse = new JSONObject(updatePriceResponse.getContentAsString());

        // Get the new price from JSON object.
        final String newPrice = updatePriceJsonResponse.getString("newPrice");

        // Validate the call returned a price in the correct currency.
        Assert.assertTrue("The price does not start with $", newPrice.startsWith("$"));

        // Put the returned price into the page.
        // Note: This is not necessary since we just want to simulate realistic traffic for the server and normally do
        // not care about client side stuff.
        HtmlPageUtils.findSingleHtmlElementByID(page, "prodPrice").setTextContent(newPrice);
        
        // (2) Get cart element slider content before adding poster to cart.
        // Read the html elements from miniCartSlider to get it's content
        List<Integer> oldProductID = new ArrayList<Integer>();
        List<String> oldFinish = new ArrayList<String>();
        List<Integer> oldWidth = new ArrayList<Integer>();
        List<Integer> oldHeight = new ArrayList<Integer>();
        List<Integer> oldCount = new ArrayList<Integer>();
        final List<HtmlElement> oldCartItems = page.getByXPath("id('miniCartMenu')//li[contains(@class, 'miniCartItem')]");           
        if (oldCartItems.size() != 0){           
            for (HtmlElement item : oldCartItems) {
                oldProductID.add(Integer.parseInt(item.getAttribute("data-prodId")));
                final List<HtmlElement> oldCartItemsAttr = item.getElementsByTagName("span");
                for (HtmlElement itemAttr : oldCartItemsAttr) {
                    if (itemAttr.getAttribute("class").equals("prodStyle")) {
                        oldFinish.add(itemAttr.getTextContent());
                    }
                    if (itemAttr.getAttribute("class").equals("prodCount")) {
                        oldCount.add(Integer.parseInt(itemAttr.getTextContent()));
                    }
                    if (itemAttr.getAttribute("class").equals("prodWidth")){
                        oldWidth.add(Integer.parseInt(itemAttr.getTextContent()));
                    }
                    if (itemAttr.getAttribute("class").equals("prodHeight")){
                        oldHeight.add(Integer.parseInt(itemAttr.getTextContent()));
                    }
                }
            }            
        };
        
        // (3) Add poster to cart.
        // Collect the request parameters.
        final List<NameValuePair> addToCartParams = new ArrayList<NameValuePair>();
        addToCartParams.add(new NameValuePair("productId", productId));
        addToCartParams.add(new NameValuePair("finish", finish));
        addToCartParams.add(new NameValuePair("size", size));

        // Perform the AJAX call and return the result.
        final WebResponse addToCartResponse = AjaxUtils.callGet(page, "/posters/addToCartSlider", addToCartParams);

        // Get JSON object from response.
        final JSONObject addToCartJsonResponse = new JSONObject(addToCartResponse.getContentAsString());

        // (4) Check that the right item was added to cart and that the product's quantity has been increased by 1 if it
        // was already in cart.
        final JSONObject product = addToCartJsonResponse.getJSONObject("product");
        Assert.assertEquals("The addToCart call returned the wrong finish.", finish, product.get("finish"));
        Assert.assertEquals("The addToCart call returned the wrong price", newPrice.substring(1), product.get("productUnitPrice"));
        Assert.assertEquals("The addToCart call returned the wrong product ID", Integer.parseInt(productId), product.getInt("productId"));

        // Get the added product's properties like ID, finish, weight, and height. This is necessary to identify the
        // product in cart for quantity validation.
        final int productIdCurrent = product.getInt("productId");
        final String finishCurrent = product.get("finish").toString();
        final JSONObject sizeCurrent = product.getJSONObject("size");
        final int widthCurrent = sizeCurrent.getInt("width");
        final int heightCurrent = sizeCurrent.getInt("height");

        // To be safe let's assume the added product was not in cart before.
        int cartItemQuantity = 0;

        // Now search the initial cart for the added product by comparing the product properties.
        for (int i = 0; i < oldCartItems.size(); i++)
        {
            // Get the initial cart product's properties.
            final int productIdBefore = oldProductID.get(i);
            final String finishBefore = oldFinish.get(i);
            final int widthBefore = oldWidth.get(i);
            final int heightBefore = oldHeight.get(i);

            // Compare with added product.
            if (productIdBefore == productIdCurrent && finishBefore.equals(finishCurrent) && widthBefore == widthCurrent &&
                heightBefore == heightCurrent)
            {
                // The product was in cart before so let's remember the initial count.
                cartItemQuantity = oldCount.get(i);

                // A product can exist in cart only once so we can stop searching.
                break;
            }
        }

        // It's expected that the item quantity for that special product has been increased by 1.
        Assert.assertEquals("The addToCart call returned the wrong count", cartItemQuantity + 1, product.getInt("productCount"));

        // (5) Publish the results of that action.
        setHtmlPage(page);
    }

    @Override
    protected void postValidate() throws Exception
    {
        // Since the AJAX calls in this action do not load a new page
        // and all the JSON responses have been validated already in the execute() method
        // there is no need for further post validation here.
    }
}
