package scripting.neg.assertElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertElementPresent_locator_nonExisting extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertElementPresent_locator_nonExisting()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * locate by index
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertElementPresent( "anc" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}