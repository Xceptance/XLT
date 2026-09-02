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
 * <p>Verifies the adresses of my account</p>
 */
public class VerifyAccountAdresses
{

    /**
     * <p>Verifies the adresses of my account</p>
     *
     * @param adressId
     * @param fullName
     * @param company
     * @param addressLine
     * @param city
     * @param state
     * @param zip
     * @param country
     */
    public static void execute(String adressId, String fullName, String company, String addressLine, String city, String state, String zip, String country)
    {
        // resolve any placeholder in the parameters
        adressId = resolve(adressId);
        fullName = resolve(fullName);
        company = resolve(company);
        addressLine = resolve(addressLine);
        city = resolve(city);
        state = resolve(state);
        zip = resolve(zip);
        country = resolve(country);
        assertText("css=#" + adressId + "  .name", fullName);
        assertText("css=#" + adressId + "  .company", company);
        assertText("css=#" + adressId + "  .addressLine", addressLine);
        assertText("css=#" + adressId + "  .city", city);
        assertText("css=#" + adressId + "  .state", state);
        assertText("css=#" + adressId + "  .zip", zip);
        assertText("css=#" + adressId + "  .country", country);

    }
}