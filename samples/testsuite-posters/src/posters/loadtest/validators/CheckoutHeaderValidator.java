/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package posters.loadtest.validators;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

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
