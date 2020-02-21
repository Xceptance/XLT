package scripting.neg.locator.xpath;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class Xpath_old extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public Xpath_old()
    {
        super(new XltDriver(true), null);
    }

    /**
     * old fanschioned xpath
     * 
     * @throws Throwable
     */
    @Test(expected = InvalidSelectorException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertXpathCount("xpath=//div[@id='page_headline']", 1);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}