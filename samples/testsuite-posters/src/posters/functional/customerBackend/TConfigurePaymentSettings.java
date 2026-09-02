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
package posters.functional.customerBackend;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AssertCreditCard;
import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.FillInPaymentForm;
import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Creates and deletes a credit card.</p>
 */
public class TConfigurePaymentSettings extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TConfigurePaymentSettings()
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

        store("xxxx xxxx xxxx 1111", "creditCardNumberClean");
        storeEval("new Date().getUTCFullYear()", "creditCardExpMonth");
        CreateRandomUser.execute();

        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        OpenAccountOverview.execute();

        //
        // ~~~ OpenPaymentSettings ~~~
        //
        startAction("OpenPaymentSettings");
        clickAndWait("id=linkPaymentOverview");
        assertElementPresent("id=titlePaymentOverview");
        //
        // ~~~ OpenFormToAddNewCreditCard ~~~
        //
        startAction("OpenFormToAddNewCreditCard");
        clickAndWait("id=linkAddNewPayment");
        FillInPaymentForm.execute("${creditCard}", "${fullName}", "${expDateMonth}", "${expDateYear}");

        //
        // ~~~ AddNewCreditCard ~~~
        //
        startAction("AddNewCreditCard");
        clickAndWait("id=btnAddPayment");
        AssertCreditCard.execute("${fullName}", "${creditCardNumberClean}", "${expDateMonth}", "${expDateYear}");

        //
        // ~~~ EditCreditCard ~~~
        //
        startAction("EditCreditCard");
        clickAndWait("css=#btnChangePayment0");
        FillInPaymentForm.execute("4111111111121234", "David Doe", "01", "${creditCardExpMonth}");

        clickAndWait("css=#btnUpdateDelAddr");
        AssertCreditCard.execute("David Doe", "xxxx xxxx xxxx 1234", "01", "${creditCardExpMonth}");

        //
        // ~~~ DeleteCreditCard ~~~
        //
        startAction("DeleteCreditCard");
        clickAndWait("css=#btnDeletePayment0");
        type("id=password", "${password}");
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeletePayment");
        assertElementPresent("id=successMessage");
        assertNotElementPresent("id=payment1");

    }

}