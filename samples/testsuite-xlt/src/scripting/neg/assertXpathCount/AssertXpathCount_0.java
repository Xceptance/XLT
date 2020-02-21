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
public class AssertXpathCount_0 extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_0()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is 0
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( PredefinedXPath.XPath, 0 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}