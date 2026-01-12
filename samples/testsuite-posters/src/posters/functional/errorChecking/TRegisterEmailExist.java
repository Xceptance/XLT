package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Verifies that an error is shown if a user wants to register with an email that already exist.</p>
 */
public class TRegisterEmailExist extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TRegisterEmailExist()
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
        // ~~~ OpenFormToRegister ~~~
        //
        startAction("OpenFormToRegister");
        clickAndWait("id=linkRegister");
        FillInRegistrationForm.execute("${lastName}", "${firstName}", "${email}", "${password}", "${password}");

        clickAndWait("id=btnRegister");
        //
        // ~~~ TryToRegister ~~~
        //
        startAction("TryToRegister");
        FillInRegistrationForm.execute("${lastName}", "${firstName}", "${email}", "${password}", "${password}");

        clickAndWait("id=btnRegister");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× An account with this email address already exists.");
        assertText("id=lastName", "${lastName}");
        assertText("id=firstName", "${firstName}");
        assertText("id=eMail", "${email}");

    }

}