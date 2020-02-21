package scripting.neg.assertNotText;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
@Ignore
public class AssertNotText_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_null()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * null value should throw exception
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		try
		{
			assertElementPresent( "id=specialchar_1" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
		assertNotText( "id=specialchar_1", null );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}