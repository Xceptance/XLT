package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class storeEval extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public storeEval()
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
        //
        // ~~~ OpenStartPage ~~~
        //
        startAction("OpenStartPage");
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        storeEval("(function(){\nvar e = document.getElementById('specialchar_1');\nreturn e != null ? e.textContent : '';\n})();", "storeEval_1");
        assertText("id=specialchar_1", resolve("${storeEval_1}"));
        assertNotText("id=specialchar_2", resolve("${storeEval_1}"));
        storeText("css=#priceText > span", "price");
        storeEval(resolve("'${price}'.replace(/([\\/\\\\^$*+?.()|[\\]{}])/g, '\\\\$1')"), "priceRex");
        assertText("id=priceText", resolve("regexpi:.*${priceRex}"));

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