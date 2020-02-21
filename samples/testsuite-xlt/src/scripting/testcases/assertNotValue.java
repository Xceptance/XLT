package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotValue extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotValue()
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

        // empty textarea
        assertNotValue("id=in_ta_1", "regexp:.+");
        // filled textarea
        assertNotValue("id=in_ta_2", "");
        // input with set value
        assertNotValue("id=in_txt_1", "");
        // input with no value
        assertNotValue("id=in_txt_5", "regexp:.+");
        // input with empty value
        assertNotValue("id=in_txt_13", "regexp:.+");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // display:none
        assertNotValue("id=invisible_display_ancestor", "");
        // visibility:invisible
        assertNotValue("id=invisible_visibility_style_ancestor", "");

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