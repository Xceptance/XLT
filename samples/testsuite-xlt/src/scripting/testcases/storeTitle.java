package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.assertTitle_frame;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class storeTitle extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public storeTitle()
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

        storeTitle("page_title");
        final assertTitle_frame _assertTitle_frame = new assertTitle_frame();
        _assertTitle_frame.execute();

        final Open_ExamplePage _open_ExamplePage0 = new Open_ExamplePage();
        _open_ExamplePage0.execute();

        assertTitle(resolve("exact:${page_title}"));

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