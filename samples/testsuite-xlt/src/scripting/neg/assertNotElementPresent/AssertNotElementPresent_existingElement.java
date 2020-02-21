package scripting.neg.assertNotElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertNotElementPresent_existingElement extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotElementPresent_existingElement()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * element exists in iframe
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotElementPresent( "id=page_headline" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}