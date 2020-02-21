package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class ContextMenu extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public ContextMenu()
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

        click("link=Misc");
        click("id=cc_clear_button");
        contextMenu("id=cm-area");
        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mousedown_content", "md");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_contextmenu_content", "cm");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mouseup_content", "mu");
        click("id=cc_clear_button");
        contextMenuAt("id=cm-area", "20, 34");
        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertNotText("id=cc_mousedown_content", resolve("exact:${md}"));
        assertNotText("id=cc_contextmenu_content", resolve("exact:${cm}"));
        assertNotText("id=cc_mouseup_content", resolve("exact:${mu}"));

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