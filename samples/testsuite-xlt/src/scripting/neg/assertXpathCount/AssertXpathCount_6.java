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
public class AssertXpathCount_6 extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_6()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is 6 (expected + 1)
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( PredefinedXPath.XPath, 6 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}