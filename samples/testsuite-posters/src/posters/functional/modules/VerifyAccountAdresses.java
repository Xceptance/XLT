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