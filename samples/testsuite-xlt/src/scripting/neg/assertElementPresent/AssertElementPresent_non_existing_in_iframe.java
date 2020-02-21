package scripting.neg.assertElementPresent;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertElementPresent_non_existing_in_iframe extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertElementPresent_non_existing_in_iframe()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * not existing in iframe but page
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		try
		{
			selectFrame( "index=0" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
		assertElementPresent( "id=page_headline" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}