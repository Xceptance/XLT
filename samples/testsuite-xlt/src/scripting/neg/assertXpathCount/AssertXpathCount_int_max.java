package scripting.neg.assertXpathCount;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.PredefinedXPath;


/**
 * 
 */
public class AssertXpathCount_int_max extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_int_max()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is max integer
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( PredefinedXPath.XPath, Integer.MAX_VALUE );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}