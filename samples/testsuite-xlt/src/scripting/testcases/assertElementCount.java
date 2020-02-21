package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.modules.StartAppear;
import scripting.modules.StartDisappear;

/**
 * TODO: Add class description
 */
public class assertElementCount extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertElementCount()
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


        //
        // ~~~ assertCount ~~~
        //
        startAction("assertCount");
        assertElementCount("id=in_txt_1", 1);
        assertElementCount("xpath=id('in_txt_1')", 1);
        assertElementCount("css=.disapp_11", 1);
        assertElementCount("name=in_txt_1", 1);
        assertElementCount("link=anc_sel1", 1);
        assertElementCount("dom=document.getElementById('in_txt_1')", 1);

        //
        // ~~~ assertNotCount ~~~
        //
        startAction("assertNotCount");
        assertNotElementCount("id=in_txt_1", 0);
        assertNotElementCount("xpath=id('in_txt_1')", 2);
        assertNotElementCount("css=.appear_11", 2);
        assertNotElementCount("name=in_txt_1", 2);
        assertNotElementCount("link=anc_sel1", 2);
        assertNotElementCount("dom=document.getElementById('in_txt_1')", 2);

        //
        // ~~~ assertNotElement ~~~
        //
        startAction("assertNotElement");
        assertNotElementCount("id=xyz", 1);
        assertNotElementCount("xpath=/xyz", 1);
        assertNotElementCount("css=#xyz", 1);
        assertNotElementCount("name=xyz", 1);
        assertNotElementCount("link=xyz", 1);
        assertNotElementCount("dom=document.getElementById('xyz')", 1);

        //
        // ~~~ waitFor ~~~
        //
        startAction("waitFor");
        final StartAppear _startAppear = new StartAppear();
        _startAppear.execute("1000");

        waitForElementCount("id=appear_1", 1);
        waitForElementCount("name=appear_2", 1);
        waitForElementCount("link=appear_3 : anchor with link name", 1);
        waitForElementCount("xpath=id('appear_5')", 1);
        waitForElementCount("dom=document.getElementById('appear_6')", 1);
        waitForElementCount("css=.appear_7", 1);

        //
        // ~~~ waitForNot ~~~
        //
        startAction("waitForNot");
        final StartDisappear _startDisappear = new StartDisappear();
        _startDisappear.execute("1000");

        waitForNotElementCount("id=disapp_1", 1);
        waitForNotElementCount("name=disapp_2", 1);
        waitForNotElementCount("link=disapp_3", 1);
        waitForNotElementCount("xpath=id('disapp_4')", 1);
        waitForNotElementCount("dom=document.getElementById('disapp_5')", 1);
        waitForNotElementCount("css=.disapp_7", 1);

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