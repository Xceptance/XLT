package scripting.neg.assertNotText;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class AssertNotText_locator_index extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public AssertNotText_locator_index()
    {
        super(new XltDriver(true), null);
    }

    /**
     * @throws Throwable
     */
    @Test(expected = InvalidSelectorException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertNotText("index=0", "*");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}