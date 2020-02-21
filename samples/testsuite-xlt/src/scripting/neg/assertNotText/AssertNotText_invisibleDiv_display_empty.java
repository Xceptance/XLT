package scripting.neg.assertNotText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertNotText_invisibleDiv_display_empty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_invisibleDiv_display_empty()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotText( "id=invisible_display", "" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}