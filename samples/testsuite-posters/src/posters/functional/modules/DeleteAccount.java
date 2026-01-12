package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.OpenAccountOverview;

/**
 * <p>Deletes a customer account.</p>
 */
public class DeleteAccount
{

    /**
     * <p>Deletes a customer account.</p>
     *
     * @param password
     */
    public static void execute(String password)
    {
        // resolve any placeholder in the parameters
        password = resolve(password);
        OpenAccountOverview.execute();

        //
        // ~~~ OpenPersonalData ~~~
        //
        startAction("OpenPersonalData");
        clickAndWait("id=linkPersonalData");
        assertElementPresent("id=titlePersonalData");
        //
        // ~~~ DeleteAccount ~~~
        //
        startAction("DeleteAccount");
        clickAndWait("id=btnDeleteAccount");
        type("id=password", password);
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeleteAccount");
        assertElementPresent("id=successMessage");

    }
}