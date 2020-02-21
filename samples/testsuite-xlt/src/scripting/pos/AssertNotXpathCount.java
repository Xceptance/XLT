package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.PredefinedXPath;


/**
 * 
 */
public class AssertNotXpathCount extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotXpathCount()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * count value is 5 (expected size)
	 * 
	 * @throws Throwable
	 */
	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotXpathCount( PredefinedXPath.XPath, 0 );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}