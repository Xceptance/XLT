package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.OpenHomepage;
import posters.functional.modules.OpenLoginForm;

/**
 * <p>Verifies that an error is shown if the user wants to log in with an email that doesn&#39;t exist.</p>
 */
public class TLoginEmailDoesntExist extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TLoginEmailDoesntExist()
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
        // ~~~ TryLogin ~~~
        //
        startAction("TryLogin");
        store("${RANDOM.String(8)}@anyserver.com", "generatedEmail");
        type("id=email", "${generatedEmail}");
        type("id=password", "wrongpassword");
        clickAndWait("id=btnSignIn");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× The email address you entered doesn't exist. Please try again.");
        assertText("id=email", "${generatedEmail}");

    }

}