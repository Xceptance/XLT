package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertStyle extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertStyle()
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


        //
        // ~~~ byStyleAttribute ~~~
        //
        startAction("byStyleAttribute");
        // inherited style only
        assertStyle("id=style_1_1", "font-size:12px");
        // own style, masked parent style
        assertStyle("id=style_1_2", "font-size:11px");
        // own style, no masked parent style
        assertStyle("id=style_1_3", "font-size:12px");

        //
        // ~~~ byIdAndClass ~~~
        //
        startAction("byIdAndClass");
        // inherited style only
        assertStyle("id=style_2_1", "font-size:12px");
        // own style, masked parent style
        assertStyle("id=style_2_2", "font-size:11px");
        // own style, no masked parent style
        assertStyle("id=style_2_3", "font-size:12px");

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