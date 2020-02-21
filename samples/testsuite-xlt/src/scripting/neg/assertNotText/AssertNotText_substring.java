package scripting.neg.assertNotText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertNotText_substring extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_substring()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * text exists
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
		assertNotText( "id=specialchar_1", "*ipsum*" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}