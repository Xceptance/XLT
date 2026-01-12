package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Verifies that an error is shown if the user types a wrong password while deleting the account.</p>
 */
public class TDeleteAccountWrongPw extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TDeleteAccountWrongPw()
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
        // ~~~ OpenFormToDeleteAccount ~~~
        //
        startAction("OpenFormToDeleteAccount");
        clickAndWait("id=btnDeleteAccount");
        //
        // ~~~ TryToDeleteAccount ~~~
        //
        startAction("TryToDeleteAccount");
        type("id=password", "wrongPassword");
        clickAndWait("id=btnDeleteAccount");
        // validate
        assertVisible("id=errorMessage");
        assertText("id=errorMessage", "× The password you entered is incorrect. Please try again.");
        assertElementPresent("id=formDeleteAccount");

    }

}