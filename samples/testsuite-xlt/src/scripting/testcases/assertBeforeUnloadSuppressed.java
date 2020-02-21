package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertBeforeUnloadSuppressed extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertBeforeUnloadSuppressed()
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
        // ~~~ ClickAnchorSectionInTOC ~~~
        //
        startAction("ClickAnchorSectionInTOC");
        click("link=anchor");
        assertText("//*[@id='anchor']/h2[5]", "anchor beforeunload");
        assertVisible("id=anc_beforeunload");
        //
        // ~~~ ClickBeforeUnloadLink ~~~
        //
        startAction("ClickBeforeUnloadLink");
        clickAndWait("id=anc_beforeunload");
        assertTitle("frame parent");

    }


    /**
     * Clean up.
     */
    @After
    public void quitDriver()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }

}