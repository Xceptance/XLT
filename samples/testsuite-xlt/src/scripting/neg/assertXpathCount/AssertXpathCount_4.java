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
public class AssertXpathCount_4 extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_4()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is 4 (expectet - 1)
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( PredefinedXPath.XPath, 4 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}