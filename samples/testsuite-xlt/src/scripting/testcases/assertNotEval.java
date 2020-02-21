package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotEval extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotEval()
    {
        super(new XltDriver(true), "http://localhost:8080");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        // whitespaces
        assertNotEval("document.getElementById('in_ta_1').value", "   ");
        // whitespaces
        assertNotEval("document.getElementById('in_ta_2').value", "in_ta_2   ");

    }


    /**
     * Clean up.
     */
    @After
    public void after()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }
}