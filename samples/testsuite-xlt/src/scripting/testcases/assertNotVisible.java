package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotVisible extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotVisible()
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

        assertNotVisible("id=in_visible_anchor_inv");
        assertNotVisible("id=invisible_visibility");
        assertNotVisible("id=invisible_visibility_style");
        assertNotVisible("id=invisible_display");
        assertNotVisible("id=invisible_hidden_input");

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