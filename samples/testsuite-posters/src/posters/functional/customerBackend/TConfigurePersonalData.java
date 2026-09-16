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

import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.Login;
import posters.functional.modules.Logout;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Changes name, email address and password of a customer.</p>
 */
public class TConfigurePersonalData extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TConfigurePersonalData()
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

        store("${RANDOM.String(8)}@anyserver.com", "generatedEmail");
        store("${password}", "generatedPassword");
        OpenLoginForm.execute();

        //
        // ~~~ StartRegistration ~~~
        //
        startAction("StartRegistration");
        clickAndWait("id=linkRegister");
        FillInRegistrationForm.execute("name", "firstName", "${generatedEmail}", "${generatedPassword}", "${generatedPassword}");

        //
        // ~~~ CreateAccount ~~~
        //
        startAction("CreateAccount");
        clickAndWait("id=btnRegister");
        Login.execute("${generatedEmail}", "${generatedPassword}", "firstName");

        OpenAccountOverview.execute();

        //
        // ~~~ OpenPersonalData ~~~
        //
        startAction("OpenPersonalData");
        clickAndWait("id=linkPersonalData");
        assertElementPresent("id=titlePersonalData");
        assertText("id=customerName", "regexp:.*firstName name");
        assertText("id=customerEmail", "regexp:.*${generatedEmail}");
        //
        // ~~~ OpenFormToChangeNameAndEmail ~~~
        //
        startAction("OpenFormToChangeNameAndEmail");
        clickAndWait("id=btnChangeNameEmail");
        assertElementPresent("id=formChangeNameEmail");
        assertText("id=lastName", "name");
        assertText("id=firstName", "firstName");
        assertText("id=eMail", "${generatedEmail}");
        //
        // ~~~ ChangeNameAndEmail ~~~
        //
        startAction("ChangeNameAndEmail");
        // store elments
        // type("id=name","newName");
        store("${RANDOM.String(5)}", "newFirstName");
        store("${RANDOM.String(5)}", "newLastName");
        type("id=firstName", "${newFirstName}");
        type("id=lastName", "${newLastName}");
        store("${RANDOM.String(8)}@anyserver.com", "newEmail");
        type("id=eMail", "${newEmail}");
        type("id=password", "${generatedPassword}");
        //
        // ~~~ Update ~~~
        //
        startAction("Update");
        clickAndWait("id=btnChangeNameEmail");
        // validate
        assertElementPresent("id=successMessage");
        assertText("id=customerFirstName", "*${newFirstName}");
        assertText("id=customerLastName", "*${newLastName}");
        assertText("id=customerEmail", "*${newEmail}");
        // assertText("//li[@id='dropdown']/a","regexp:.*${newFirstName}");
        //
        // ~~~ OpenFormToChangePassword ~~~
        //
        startAction("OpenFormToChangePassword");
        clickAndWait("id=btnChangePassword");
        type("id=oldPassword", "${generatedPassword}");
        store("secret4ever", "newPassword");
        type("id=password", "${newPassword}");
        type("id=passwordAgain", "${newPassword}");
        //
        // ~~~ ChangePassword ~~~
        //
        startAction("ChangePassword");
        clickAndWait("id=btnChangePassword");
        assertElementPresent("id=successMessage");
        Logout.execute();

        OpenLoginForm.execute();

        //
        // ~~~ TryLoginWithOldPassword ~~~
        //
        startAction("TryLoginWithOldPassword");
        type("id=email", "${newEmail}");
        type("id=password", "${generatedPassword}");
        clickAndWait("id=btnSignIn");
        assertElementPresent("id=errorMessage");
        assertElementPresent("id=showUserMenu");
        Login.execute("${newEmail}", "${newPassword}", "${newFirstName}");


    }

}