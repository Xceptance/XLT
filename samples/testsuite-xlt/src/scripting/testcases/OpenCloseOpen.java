package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

/**
 * Related to #1728
 Close the last open window/tab and open new page.
 */
public class OpenCloseOpen extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public OpenCloseOpen()
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
        open("/testpages/examplePage_1.html");
        assertTitle("example page");
        close();
        open("/testpages/examplePage_1.html");
        assertTitle("example page");

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