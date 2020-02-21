package scripting.neg.assertElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertElementPresent_non_existing extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertElementPresent_non_existing()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * non existing ID
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertElementPresent( "id=anc" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}