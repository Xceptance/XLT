package scripting.neg.assertNotXpathCount;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class AssertNotXpathCount_locator_xpath extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public AssertNotXpathCount_locator_xpath()
    {
        super(new XltDriver(true), null);
    }

    /**
     * located by link
     * 
     * @throws Throwable
     */
    @Test(expected = InvalidSelectorException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertNotXpathCount("xpath=id('anc_sel1')", 0);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}