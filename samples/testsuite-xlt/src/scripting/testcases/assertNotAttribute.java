package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotAttribute extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotAttribute()
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

        // 1 vs 2 whitespaces
        assertNotAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", " ");
        // 2 vs 1 whitespace
        assertNotAttribute("xpath=id('select_17')/option[@title='1 space']@value", "  ");
        // empty attribute value must not match any sign
        assertNotAttribute("xpath=id('in_txt_13')@value", "regexp:.+");
        // substring (max length n-1) must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "foo");
        // any single character must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "?");

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