package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotClass extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotClass()
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
        assertNotClass("xpath=//div[@id='appear' and contains(@class,'cat')]/..", "cat");
        assertNotClass("css= input[value='appear']", "cat");
        assertNotClass("xpath=id('anchor_list')", "anchor_list");
        assertNotClass("//div[@id='anchor_list']/ol[1]/li[2]", "a");

        //
        // ~~~ MultipleClassesToMatch ~~~
        //
        startAction("MultipleClassesToMatch");
        assertNotClass("id=common_confirmation_area", "confirmation area common area");
        assertNotClass("dom=document.getElementById('common_confirmation_area')", "common_confirmation");

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