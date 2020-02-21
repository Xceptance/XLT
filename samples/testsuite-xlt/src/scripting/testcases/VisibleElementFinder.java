package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

/**
 * Locator points to visible AND invisible elements. The invisible one is listed before the visible one.
 The element finder must choose the visible (second) one.
 */
public class VisibleElementFinder extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public VisibleElementFinder()
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
        final scripting.modules.VisibleElementFinder _visibleElementFinder = new scripting.modules.VisibleElementFinder();
        _visibleElementFinder.execute();


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