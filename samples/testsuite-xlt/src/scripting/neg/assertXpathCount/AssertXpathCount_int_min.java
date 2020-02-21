package scripting.neg.assertXpathCount;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;

import scripting.util.PageOpener;
import scripting.util.PredefinedXPath;

/**
 * 
 */
public class AssertXpathCount_int_min extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public AssertXpathCount_int_min()
    {
        super(new XltDriver(true), null);
    }

    /**
     * count value is min integer
     * 
     * @throws Throwable
     */
    @Test(expected = XltException.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertXpathCount(PredefinedXPath.XPath, Integer.MIN_VALUE);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
