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