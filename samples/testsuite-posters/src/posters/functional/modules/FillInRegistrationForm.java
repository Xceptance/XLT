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

/**
 * <p>Fills in the complete registration form.</p>
 */
public class FillInRegistrationForm
{

    /**
     * <p>Fills in the complete registration form.</p>
     *
     * @param lastName
     * @param firstName
     * @param email
     * @param password
     * @param passwordAgain
     */
    public static void execute(String lastName, String firstName, String email, String password, String passwordAgain)
    {
        // resolve any placeholder in the parameters
        lastName = resolve(lastName);
        firstName = resolve(firstName);
        email = resolve(email);
        password = resolve(password);
        passwordAgain = resolve(passwordAgain);
        type("id=lastName", lastName);
        type("id=firstName", firstName);
        type("id=eMail", email);
        type("id=password", password);
        type("id=passwordAgain", passwordAgain);

    }
}