package scripting.neg.assertNotElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertNotElementPresent_iframe extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotElementPresent_iframe()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * element exists
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		try
		{
			selectFrame( "dom=frames[\"iframe1\"].frames[\"iframe2\"]" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
		assertNotElementPresent( "id=f2" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}