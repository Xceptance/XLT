package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertClass extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertClass()
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
        // ~~~ SingleClassToMatch ~~~
        //
        startAction("SingleClassToMatch");
        assertClass("xpath=//div[@id='appear' and contains(@class,'cat')][1]", "cat");
        assertClass("//ol[@id='mainList']/li[4]/div", "cat");
        assertClass("xpath=id('appear')", "cat");
        assertClass("//div[@id='anchor_list']/ol[1]/li[1]", "a");

        //
        // ~~~ MultipleClassesToMatch ~~~
        //
        startAction("MultipleClassesToMatch");
        assertClass("id=common_confirmation_area", "confirmation_area common_confirmation_area");
        assertClass("dom=document.getElementById('common_confirmation_area')", "confirmation_area");
        assertClass("css=#common_confirmation_area", "common_confirmation_area");

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