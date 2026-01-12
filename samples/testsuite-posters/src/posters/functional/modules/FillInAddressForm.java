package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Fills in the complete address form.</p>
 */
public class FillInAddressForm
{

    /**
     * <p>Fills in the complete address form.</p>
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
        type("id=fullName", name);
        type("id=company", company);
        type("id=addressLine", address);
        type("id=city", city);
        type("id=state", state);
        type("id=zip", zip);
        select("id=country", "label=" + country);

    }
}