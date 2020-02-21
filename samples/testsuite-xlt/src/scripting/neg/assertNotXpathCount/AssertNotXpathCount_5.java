package scripting.neg.assertNotXpathCount;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.PredefinedXPath;


/**
 * 
 */
public class AssertNotXpathCount_5 extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotXpathCount_5()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is 5 (expected size)
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotXpathCount( PredefinedXPath.XPath, 5 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}