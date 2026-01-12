package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.FillInPaymentForm;
import posters.functional.modules.OpenAccountOverview;

/**
 * <p>Create a random payment methode for a sign in user.</p>
 */
public class AddRandomPaymentToAccount
{

    /**
     * <p>Create a random payment methode for a sign in user.</p>
     *
     */
    public static void execute()
    {
        // Add some random characters to the input string.
        store("${fullName}${RANDOM.String(${RANDOM.Number(2,6)})}", "newPaymentFullName");
        OpenAccountOverview.execute();

        //
        // ~~~ OpenPaymentOverview ~~~
        //
        startAction("OpenPaymentOverview");
        clickAndWait("id=linkPaymentOverview");
        //
        // ~~~ OpenFormToEnterNewPayment ~~~
        //
        startAction("OpenFormToEnterNewPayment");
        clickAndWait("id=linkAddNewPayment");
        FillInPaymentForm.execute("${creditCard}", "${newPaymentFullName}", "${expDateMonth}", "${expDateYear}");

        //
        // ~~~ AddNewShippingAddress ~~~
        //
        startAction("AddNewShippingAddress");
        clickAndWait("id=btnAddPayment");
        assertElementPresent("id=successMessage");

    }
}