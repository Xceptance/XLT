/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package posters.loadtest.tests;

import org.junit.Test;

import posters.loadtest.actions.AddToCart;
import posters.loadtest.actions.Homepage;
import posters.loadtest.actions.order.PlaceOrder;
import posters.loadtest.actions.order.ViewCart;
import posters.loadtest.flows.BrowsingFlow;
import posters.loadtest.flows.CheckoutFlow;
import posters.loadtest.util.Account;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Open the landing page and browse the catalog to a random product. Configure this product and add it to the cart.
 * Finally process the checkout as guest including the final order placement step.
 */
public class TGuestOrder extends AbstractTestCase
{
    /**
     * Main test method.
     *
     * @throws Throwable
     */
    @Test
    public void guestOrder() throws Throwable
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

        // The probability to select a top category during browsing
        final int topCategoryProbability = getProperty("browsing.topCategoryProbability", 0);

        // The min. number of products to browse, search and add to cart
        final int productsMin = getProperty("products.min", 0);

        // The max. number of products to browse, search and add to cart
        final int productsMax = getProperty("products.max", 0);

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();
        previousAction = homepage;

        // select randomly the number of products to browse
        final int numberOfProducts = XltRandom.nextInt(productsMin, productsMax);
        for (int round = 0; round < numberOfProducts; round++)
        {
            // Browse the catalog and view a product detail page.
            // The browsing is encapsulated in a flow that combines a sequence of several XLT actions.
            // Different test cases can call this method now to reuse the flow.
            // This is a concept for code structuring you can implement if needed, yet explicit support
            // is neither available in the XLT framework nor necessary when you manually create a flow.
            final BrowsingFlow browsingFlow = new BrowsingFlow(previousAction, topCategoryProbability, pagingProbability, pagingMin,
                                                               pagingMax);
            previousAction = browsingFlow.run();

            // Configure the product (size and finish) and add it to cart
            final AddToCart addToCart = new AddToCart(previousAction);
            addToCart.run();
            previousAction = addToCart;

        }

        // go to the cart overview page
        final ViewCart viewCart = new ViewCart(previousAction);
        viewCart.run();
        previousAction = viewCart;

        // Checkout Flow
        final CheckoutFlow checkoutFlow = new CheckoutFlow(previousAction, new Account());
        final AbstractHtmlPageAction enterPaymentMethod = checkoutFlow.run();
        previousAction = enterPaymentMethod;

        // place the order
        final PlaceOrder placeOrder = new PlaceOrder(previousAction);
        placeOrder.run();
    }
}
