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

import posters.functional.modules.OpenAccountOverview;

/**
 * <p>Deletes a customer account.</p>
 */
public class DeleteAccount
{

    /**
     * <p>Deletes a customer account.</p>
     *
     * @param password
     */
    public static void execute(String password)
    {
        // resolve any placeholder in the parameters
        password = resolve(password);
        OpenAccountOverview.execute();

        //
        // ~~~ OpenPersonalData ~~~
        //
        startAction("OpenPersonalData");
        clickAndWait("id=linkPersonalData");
        assertElementPresent("id=titlePersonalData");
        //
        // ~~~ DeleteAccount ~~~
        //
        startAction("DeleteAccount");
        clickAndWait("id=btnDeleteAccount");
        type("id=password", password);
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeleteAccount");
        assertElementPresent("id=successMessage");

    }
}