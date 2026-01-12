package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.Login;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Simulates customer registration.</p>
 */
public class TRegister extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TRegister()
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

        CreateRandomUser.execute();

        Login.execute("${generatedEmail}", "${password}", "${firstName}");


    }

}