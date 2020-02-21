package scripting.neg.assertElementPresent;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class AssertElementPresent_locator_index extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertElementPresent_locator_index()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * locate by index
	 * 
	 * @throws Throwable
	 */
	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertElementPresent( "index=0" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}