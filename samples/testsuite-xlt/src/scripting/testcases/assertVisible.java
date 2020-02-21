package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertVisible extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertVisible()
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

        assertVisible("id=in_visible_anchor_vis");
        assertVisible("xpath=/html");
        assertVisible("xpath=id('select')/table[1]");
        assertVisible("name=anc_sel1");
        assertVisible("link=anc_sel1");
        assertVisible("id=in_txt_1");
        assertVisible("id=in_chk_1");
        assertVisible("id=fileInput");
        // assertVisible("id=invisible_empty_div");
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