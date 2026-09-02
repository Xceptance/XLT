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
 * <p>Verifies the values of the input fields of the address form.</p>
 */
public class VerifyAddressForm
{

    /**
     * <p>Verifies the values of the input fields of the address form.</p>
     *
     * @param name
     * @param company
     * @param address
     * @param city
     * @param state
     * @param zip
     * @param country
     */
    public static void execute(String name, String company, String address, String city, String state, String zip, String country)
    {
        // resolve any placeholder in the parameters
        name = resolve(name);
        company = resolve(company);
        address = resolve(address);
        city = resolve(city);
        state = resolve(state);
        zip = resolve(zip);
        country = resolve(country);
        assertText("id=fullName", name);
        assertText("id=company", company);
        assertText("id=addressLine", address);
        assertText("id=city", city);
        assertText("id=state", state);
        assertText("id=zip", zip);
        assertSelectedValue("id=country", country);

    }
}