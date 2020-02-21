package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertValue extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertValue()
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
        assertValue("id=in_ta_1", "");
        // filled textarea
        assertValue("id=in_ta_2", "in_ta_2");
        // input with set value
        assertValue("id=in_txt_1", "in_txt_1");
        // input with no value
        assertValue("id=in_txt_5", "");
        // input with empty value
        assertValue("id=in_txt_13", "");
        // hidden input
        assertValue("id=invisible_hidden_input", "invisible_hidden_input");
        // display:none
        assertValue("id=invisible_display_ancestor", "invisible_display_ancestor");
        // visibility:invisible
        assertValue("id=invisible_visibility_style_ancestor", "invisible_visibility_style_ancestor");
        // misc
        assertValue("xpath=/html/body", "");

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