package posters.functional.customerBackend;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.DeleteAccount;
import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.Login;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Registers a new customer and deletes them.</p>
 */
public class TDeleteCustomer extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TDeleteCustomer()
    {
        super("https://localhost:8443");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        OpenHomepage.execute();

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
        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        DeleteAccount.execute("${password}");

        OpenLoginForm.execute();

        //
        // ~~~ TryToLoginAgain ~~~
        //
        startAction("TryToLoginAgain");
        type("id=email", "${generatedEmail}");
        type("id=password", "${password}");
        clickAndWait("id=btnSignIn");
        assertElementPresent("id=errorMessage");

    }

}