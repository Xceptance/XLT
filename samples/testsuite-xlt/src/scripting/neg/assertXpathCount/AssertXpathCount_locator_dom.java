package scripting.neg.assertXpathCount;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class AssertXpathCount_locator_dom extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public AssertXpathCount_locator_dom()
    {
        super(new XltDriver(true), null);
    }

    /**
     * located by dom
     * 
     * @throws Throwable
     */
    @Test(expected = InvalidSelectorException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertXpathCount("dom=frames[\"iframe1\"]", 1);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}