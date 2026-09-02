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

import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.FillInAddressForm;
import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.VerifyAccountAdresses;
import posters.functional.modules.VerifyAddressForm;

/**
 * <p>Updates, creates and removes a shipping and a billing address.</p>
 */
public class TAdjustAddress extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TAdjustAddress()
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

        CreateRandomUser.execute();

        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        OpenAccountOverview.execute();

        //
        // ~~~ OpenAddressOverview ~~~
        //
        startAction("OpenAddressOverview");
        clickAndWait("id=linkAddressOverview");
        assertElementPresent("id=titleDelAddr");
        //
        // ~~~ OpenFormToEnterNewShippingAddress ~~~
        //
        startAction("OpenFormToEnterNewShippingAddress");
        clickAndWait("id=linkAddNewShipAddr");
        FillInAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        //
        // ~~~ AddNewShippingAddress ~~~
        //
        startAction("AddNewShippingAddress");
        clickAndWait("id=btnAddShippAddr");
        assertElementPresent("id=successMessage");
        VerifyAccountAdresses.execute("shippingAddr0", "${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        //
        // ~~~ OpenFormToUpdateShippingAddress ~~~
        //
        startAction("OpenFormToUpdateShippingAddress");
        clickAndWait("id=btnChangeAddr0");
        VerifyAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        FillInAddressForm.execute("name", "company", "address", "city", "state", "67890", "Germany");

        //
        // ~~~ UpdateShippingAddress ~~~
        //
        startAction("UpdateShippingAddress");
        clickAndWait("id=btnUpdateDelAddr");
        assertElementPresent("id=successMessage");
        VerifyAccountAdresses.execute("shippingAddr0", "name", "company", "address", "city", "state", "67890", "Germany");

        //
        // ~~~ DeleteShippingAddress ~~~
        //
        startAction("DeleteShippingAddress");
        clickAndWait("id=btnDeleteAddr0");
        type("id=password", "${password}");
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeleteAddress");
        assertElementPresent("id=successMessage");
        assertNotElementPresent("id=btnDeleteAddr1");
        //
        // ~~~ OpenFormToEnterNewBillingAddress ~~~
        //
        startAction("OpenFormToEnterNewBillingAddress");
        clickAndWait("id=linkAddNewBillAddr");
        FillInAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        //
        // ~~~ AddNewBillingAddress ~~~
        //
        startAction("AddNewBillingAddress");
        clickAndWait("id=btnAddBillAddr");
        assertElementPresent("id=successMessage");
        VerifyAccountAdresses.execute("billAddr0", "${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        //
        // ~~~ OpenFormToUpdateBillingAddress ~~~
        //
        startAction("OpenFormToUpdateBillingAddress");
        clickAndWait("id=btnChangeBillAddr0");
        VerifyAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        FillInAddressForm.execute("name", "company", "address", "city", "state", "67890", "Germany");

        //
        // ~~~ UpdateBillingAddress ~~~
        //
        startAction("UpdateBillingAddress");
        clickAndWait("id=btnUpdateBillAddr");
        assertElementPresent("id=successMessage");
        // name.*company.*address.*city, .*
        VerifyAccountAdresses.execute("billAddr0", "name", "company", "address", "city", "state", "67890", "Germany");

        //
        // ~~~ DeleteBillingAddress ~~~
        //
        startAction("DeleteBillingAddress");
        clickAndWait("id=btnDeleteBillAddr0");
        type("id=password", "${password}");
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeleteAddress");
        assertElementPresent("id=successMessage");
        assertNotElementPresent("id=billAddr1");

    }

}