package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.FillInAddressForm;
import posters.functional.modules.OpenAccountOverview;

/**
 * <p>Create a random shipping adress for a sign in user.</p>
 */
public class AddRandomShippingAdressToAccount
{

    /**
     * <p>Create a random shipping adress for a sign in user.</p>
     *
     */
    public static void execute()
    {
        // Add some random characters to the input string.
        store("${fullName}${RANDOM.String(${RANDOM.Number(1,4)})}", "newShipFullName");
        // Add some random characters to the input string.
        store("${company}${RANDOM.String(${RANDOM.Number(1,4)})}", "newShipCompany");
        // Add some random characters to the input string.
        store("${address}${RANDOM.String(${RANDOM.Number(1,4)})}", "newShipAddress");
        // Add some random characters to the input string.
        store("${city}${RANDOM.String(${RANDOM.Number(1,4)})}", "newShipCity");
        OpenAccountOverview.execute();

        //
        // ~~~ OpenAddressOverview ~~~
        //
        startAction("OpenAddressOverview");
        clickAndWait("id=linkAddressOverview");
        //
        // ~~~ OpenFormToEnterNewShippingAddress ~~~
        //
        startAction("OpenFormToEnterNewShippingAddress");
        clickAndWait("id=linkAddNewShipAddr");
        FillInAddressForm.execute("${newShipFullName}", "${newShipCompany}", "${newShipAddress}", "${newShipCity}", "${state}", "${zip}", "${country}");

        //
        // ~~~ AddNewShippingAddress ~~~
        //
        startAction("AddNewShippingAddress");
        clickAndWait("id=btnAddShippAddr");
        assertElementPresent("id=successMessage");

    }
}