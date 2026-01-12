package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.FillInRegistrationForm;
import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Verifies that an error is shown if a customer wants to change the email to an email that aleady exist.</p>
 */
public class TChangeToExistingEmail extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TChangeToExistingEmail()
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

        // First register a new customer
        OpenLoginForm.execute();

        //
        // ~~~ StartRegistration ~~~
        //
        startAction("StartRegistration");
        clickAndWait("id=linkRegister");
        assertElementPresent("id=formRegister");
        store("${RANDOM.String(8)}", "randomLastName");
        store("${RANDOM.String(8)}", "randomFirstName");
        store("${RANDOM.String(8)}@anyserver.com", "randomEmail");
        FillInRegistrationForm.execute("${randomLastName}", "${randomFirstName}", "${randomEmail}", "${password}", "${password}");

        //
        // ~~~ Register ~~~
        //
        startAction("Register");
        clickAndWait("id=btnRegister");
        assertElementPresent("id=successMessage");
        // Now try to change John Doe's email to the one of the just created customer
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
        //
        // ~~~ TryToUpdateAccount ~~~
        //
        startAction("TryToUpdateAccount");
        type("id=eMail", "${randomEmail}");
        type("id=password", "${password}");
        clickAndWait("id=btnChangeNameEmail");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× An account with this email address already exists.");
        assertText("id=lastName", "${lastName}");
        assertText("id=firstName", "${firstName}");
        assertText("id=eMail", "${email}");

    }

}