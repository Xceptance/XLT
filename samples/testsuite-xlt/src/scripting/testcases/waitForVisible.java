package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class waitForVisible extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public waitForVisible()
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

        type("id=timeout_field", "2000");
        click("id=invisible_visibility_show");
        waitForVisible("id=invisible_visibility_ancestor");
        click("id=invisible_visibility_hide");
        waitForNotVisible("id=invisible_visibility_ancestor");
        click("id=invisible_visibility_style_show");
        waitForVisible("id=invisible_visibility_style_ancestor");
        click("id=invisible_visibility_style_hide");
        waitForNotVisible("id=invisible_visibility_style_ancestor");
        click("id=invisible_display_show");
        waitForVisible("id=invisible_display_ancestor");
        click("id=invisible_display_hide");
        waitForNotVisible("id=invisible_display_ancestor");
        click("id=invisible_css_submit_show");
        waitForVisible("id=invisible_css_submit");
        click("id=invisible_css_submit_hide");
        waitForNotVisible("id=invisible_css_submit");
        click("id=invisible_checkbox_byDisplayNone_show");
        waitForVisible("id=invisible_checkbox_byDisplayNone");
        click("id=invisible_checkbox_byDisplayNone_hide");
        waitForNotVisible("id=invisible_checkbox_byDisplayNone");
        click("id=invisible_radio_byDisplayNone_show");
        waitForVisible("id=invisible_radio_byDisplayNone");
        click("id=invisible_radio_byDisplayNone_hide");
        waitForNotVisible("id=invisible_radio_byDisplayNone");

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