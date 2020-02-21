package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class Texttransform extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public Texttransform()
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

        click("link=Text Transform");
        assertText("//*[@id='text-transform']/p[contains(@class,'upcase')][1]", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS.");
        assertText("//*[@id='text-transform']/p[contains(@class,'locase')][1]", "this text should be displayed in small letters.");
        assertText("//*[@id='text-transform']/p[contains(@class,'capital')][1]", "This Text Should Be Displayed In Capitalized Form.");
        assertText("id=text-transform", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS. this text should be displayed in small letters. This Text Should Be Displayed In Capitalized Form.");
        assertNotText("xpath=id('text-transform')/p[@class='upcase']", "This text should be displayed in capital letters.");
        assertNotText("xpath=id('text-transform')/p[@class='locase']", "THIS TEXT SHOULD BE DISPLAYED IN SMALL LETTERS.");
        assertNotText("xpath=id('text-transform')/p[@class='capital']", "this text should be displayed in capitalized form.");

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