package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Create a random user.</p>
 */
public class CreateRandomUser
{

    /**
     * <p>Create a random user.</p>
     *
     */
    public static void execute()
    {
        OpenLoginForm.execute();

        //
        // ~~~ StartRegistration ~~~
        //
        startAction("StartRegistration");
        clickAndWait("id=linkRegister");
        assertElementPresent("id=formRegister");
        store("${RANDOM.String(8)}@anyserver.com", "generatedEmail");
        FillInRegistrationForm.execute("${lastName}", "${firstName}", "${generatedEmail}", "${password}", "${password}");

        //
        // ~~~ Register ~~~
        //
        startAction("Register");
        clickAndWait("id=btnRegister");
        assertElementPresent("id=successMessage");

    }
}