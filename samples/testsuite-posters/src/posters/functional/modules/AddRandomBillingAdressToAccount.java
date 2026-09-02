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
package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.FillInAddressForm;
import posters.functional.modules.OpenAccountOverview;

/**
 * <p>Create a random billing adress for a sign in user.</p>
 */
public class AddRandomBillingAdressToAccount
{

    /**
     * <p>Create a random billing adress for a sign in user.</p>
     *
     */
    public static void execute()
    {
        // Add some random characters to the input string.
        store("${fullName}${RANDOM.String(${RANDOM.Number(1,4)})}", "newBillFullName");
        // Add some random characters to the input string.
        store("${company}${RANDOM.String(${RANDOM.Number(1,4)})}", "newBillCompany");
        // Add some random characters to the input string.
        store("${address}${RANDOM.String(${RANDOM.Number(1,4)})}", "newBillAddress");
        // Add some random characters to the input string.
        store("${city}${RANDOM.String(${RANDOM.Number(1,4)})}", "newBillCity");
        OpenAccountOverview.execute();

        //
        // ~~~ OpenAddressOverview ~~~
        //
        startAction("OpenAddressOverview");
        clickAndWait("id=linkAddressOverview");
        //
        // ~~~ OpenFormToEnterNewBillingAddress ~~~
        //
        startAction("OpenFormToEnterNewBillingAddress");
        clickAndWait("id=linkAddNewBillAddr");
        FillInAddressForm.execute("${newBillFullName}", "${newBillCompany}", "${newBillAddress}", "${newBillCity}", "${state}", "${zip}", "${country}");

        //
        // ~~~ AddNewBillingAddress ~~~
        //
        startAction("AddNewBillingAddress");
        clickAndWait("id=btnAddBillAddr");
        assertElementPresent("id=successMessage");

    }
}