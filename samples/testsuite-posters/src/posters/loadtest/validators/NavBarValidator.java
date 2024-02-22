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
package posters.loadtest.validators;

import java.util.Iterator;
import java.util.List;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.util.HtmlPageUtils;

/**
 * Checks for the correct header elements
 */
public class NavBarValidator
{
    /**
     * Make a stateless singleton available.
     */
    private static final NavBarValidator instance = new NavBarValidator();

    /**
     * Checks the poster store side navigation elements
     * 
     * @param page
     *            the page to check
     */
    public void validate(final HtmlPage page) throws Exception
    {

        // Check that the category menu contains at least two top categories
        // For this purpose we get a list of all top categories and check the
        // size of the list
        final List<HtmlElement> topCategories = HtmlPageUtils.findHtmlElements(page, "id('categoryMenu')/ul/li[@class='dropdown header-menu-item']");
        Assert.assertTrue("There are less then two top categories in the side nav.", topCategories.size() >= 2);

        // Check that each top category has at least one drop down item
        // category
        for (final Iterator<HtmlElement> iterator = topCategories.iterator(); iterator.hasNext();)
      {
            final HtmlElement htmlElement = iterator.next();
            // relative xpath to address the first sibling after the top
            // category that is a drop down item
            Assert.assertTrue("Top category is not followed by a level-1 category.",
                              HtmlPageUtils.isElementPresent(htmlElement, "./div/child::ul[@class='dropdown-menu']/li/a"));
        }

    }

    /**
     * The instance for easy reuse. Possible because this validator is stateless.
     * 
     * @return the instance
     */
    public static NavBarValidator getInstance()
    {
        return instance;
    }
}
