package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Verifies that an error is shown if the user wants to update the email and types a wrong password.</p>
 */
public class TChangeEmailWrongPassword extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TChangeEmailWrongPassword()
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

        Login.execute("${email}", "${password}", "${firstName}");

        OpenAccountOverview.execute();

        //
        // ~~~ OpenPersonalData ~~~
        //
        startAction("OpenPersonalData");
        clickAndWait("id=linkPersonalData");
        //
        // ~~~ OpenFormToChangeEmail ~~~
        //
        startAction("OpenFormToChangeEmail");
        clickAndWait("id=btnChangeNameEmail");
        store("${RANDOM.String(8)}@anyserver.com", "generatedEmail");
        //
        // ~~~ TryToUpdateAccount ~~~
        //
        startAction("TryToUpdateAccount");
        type("id=eMail", "${generatedEmail}");
        type("id=password", "wrongPassword");
        clickAndWait("id=btnChangeNameEmail");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× The password you entered is incorrect. Please try again.");
        assertText("id=lastName", "${lastName}");
        assertText("id=firstName", "${firstName}");
        assertText("id=eMail", "${email}");

    }

}