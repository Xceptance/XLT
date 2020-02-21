package scripting.neg.assertElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertElementPresent_existing_in_iframe extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertElementPresent_existing_in_iframe()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * not existing in page but in iframe
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertElementPresent( "id=f1" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}