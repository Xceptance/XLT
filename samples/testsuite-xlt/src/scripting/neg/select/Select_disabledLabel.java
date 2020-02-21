package scripting.neg.select;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidElementStateException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class Select_disabledLabel extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public Select_disabledLabel()
    {
        super(new XltDriver(true), null);
    }

    @Test(expected = InvalidElementStateException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        select("id=select_22", "label=select_22_b");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}