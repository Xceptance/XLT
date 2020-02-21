package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class storeElementCount extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public storeElementCount()
    {
        super(new XltDriver(true), "http://localhost:8080/");
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

        storeElementCount("xpath=id('xyz')", "notexisting");
        assertElementCount("xpath=id('xyz')", Integer.parseInt(resolve("${notexisting}")));
        storeElementCount("xpath=id('xpath_count')/input", "existing");
        assertElementCount("xpath=id('xpath_count')/input", Integer.parseInt(resolve("${existing}")));

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