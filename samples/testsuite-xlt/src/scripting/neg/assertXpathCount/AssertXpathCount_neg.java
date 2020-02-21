package scripting.neg.assertXpathCount;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;
import scripting.util.PredefinedXPath;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class AssertXpathCount_neg extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_neg()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * negative count value
	 * 
	 * @throws Throwable
	 */
	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( PredefinedXPath.XPath, -1 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}