package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.Browse;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Simulates browsing the catalog.</p>
 */
public class TBrowse extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TBrowse()
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

        Browse.execute();

        Browse.execute();

        Browse.execute();


    }

}