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

import posters.functional.modules.OpenLoginForm;

/**
 * <p>Logs a user in using the specified credentials.</p>
 */
public class Login
{

    /**
     * <p>Logs a user in using the specified credentials.</p>
     *
     * @param email
     * @param password
     * @param firstName
     */
    public static void execute(String email, String password, String firstName)
    {
        // resolve any placeholder in the parameters
        email = resolve(email);
        password = resolve(password);
        firstName = resolve(firstName);
        OpenLoginForm.execute();

        //
        // ~~~ Login ~~~
        //
        startAction("Login");
        type("id=email", email);
        type("id=password", password);
        clickAndWait("id=btnSignIn");
        assertVisible("id=successMessage");
        assertText("id=successMessage", "× Login successful. Have fun in our shop!");

    }
}