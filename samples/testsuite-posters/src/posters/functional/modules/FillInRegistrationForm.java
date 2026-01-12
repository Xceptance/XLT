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