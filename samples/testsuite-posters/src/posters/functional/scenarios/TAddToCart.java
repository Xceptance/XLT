/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AddToCart;
import posters.functional.modules.CalcAndStoreCartItemTotalUnitPrice;
import posters.functional.modules.ConfirmDelete;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.VerifyCartItem;
import posters.functional.modules.VerifyCartOverview;
import posters.functional.modules.VerifyMiniCartElement;

/**
 * <p>Simulates browsing the catalog and adding product to cart.</p>
 */
public class TAddToCart extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TAddToCart()
    {
        super("https://localhost:8443");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        OpenHomepage.execute();

        AddToCart.execute();

        VerifyCartItem.execute("0", "${productName}", "1", "${productFinish}", "${productSize}");

        // totalPrice
        storeText("id=orderTotal", "orderTotal");
        VerifyMiniCartElement.execute("1", "${productName}", "${productFinish}", "1", "${productSize}", "${productPrice}");

        VerifyCartOverview.execute("1");

        AddToCart.execute();

        VerifyCartItem.execute("0", "${productName}", "1", "${productFinish}", "${productSize}");

        CalcAndStoreCartItemTotalUnitPrice.execute("0", "$", "proPrice_new", "productTotalPrice");

        storeText("id=orderTotal", "totalPrice");
        VerifyMiniCartElement.execute("1", "${productName}", "${productFinish}", "1", "${productSize}", "${productPrice}");

        VerifyCartOverview.execute("2");

        //
        // ~~~ UpdateProductCount ~~~
        //
        startAction("UpdateProductCount");
        storeText("id=orderTotal", "oldOrderTotal");
        type("css=#product0 .productCount", "3");
        // clickUpdateButton
        // //tr[@id='product0']/td[3]/form/div/button[@class='btnUpdateProduct']
        click("css=#product0 .btnUpdateProduct");
        waitForNotText("id=orderTotal", "${oldOrderTotal}");
        assertText("css=#product0 .productCount", "3");
        storeText("id=orderTotal", "totalPrice");
        CalcAndStoreCartItemTotalUnitPrice.execute("0", "$", "proPrice_new", "productTotalPriceUpdate");

        VerifyMiniCartElement.execute("1", "${productName}", "${productFinish}", "3", "${productSize}", "${productTotalPriceUpdate}");

        VerifyCartOverview.execute("4");

        //
        // ~~~ RemoveProduct ~~~
        //
        startAction("RemoveProduct");
        click("css=#btnRemoveProdCount1");
        ConfirmDelete.execute();

        waitForNotElementPresent("css=#product1");
        assertText("css=#product0 .productCount", "3");
        storeText("id=orderTotal", "orderTotal");
        VerifyCartOverview.execute("3");

        //
        // ~~~ ShowProductOfCart ~~~
        //
        startAction("ShowProductOfCart");
        storeText("css=#product0 .productName", "productName");
        storeText("css=#product0 .productStyle", "productFinish");
        clickAndWait("//*[@id='product0']//img");
        assertText("id=titleProductName", "${productName}");
        //
        // ~~~ AddSameProduct ~~~
        //
        startAction("AddSameProduct");
        check("//div[@id='selectStyle']/div[@class='radio'][${finishIndex}+1]/label/input");
        select("id=selectSize", "label=${productSize}");
        click("id=btnAddToCart");
        VerifyCartItem.execute("0", "${productName}", "4", "${productFinish}", "${productSize}");

        storeText("id=orderTotal", "orderTotal");
        VerifyCartOverview.execute("4");


    }

}