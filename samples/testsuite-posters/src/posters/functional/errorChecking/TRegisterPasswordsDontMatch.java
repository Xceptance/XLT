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
package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Verifies that an error is shown if a user wants to register and the entered passwords don&#39;t match.</p>
 */
public class TRegisterPasswordsDontMatch extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TRegisterPasswordsDontMatch()
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

        OpenLoginForm.execute();

        //
        // ~~~ OpenFormToRegister ~~~
        //
        startAction("OpenFormToRegister");
        clickAndWait("id=linkRegister");
        store("${RANDOM.String(8)}@anyserver.com", "generatedEmail");
        FillInRegistrationForm.execute("${lastName}", "${firstName}", "${generatedEmail}", "${password}", "wrongPassword");

        //
        // ~~~ TryToRegister ~~~
        //
        startAction("TryToRegister");
        clickAndWait("id=btnRegister");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× The passwords you entered don't match. Please try again.");
        assertText("id=lastName", "${lastName}");
        assertText("id=firstName", "${firstName}");
        assertText("id=eMail", "${generatedEmail}");

    }

}