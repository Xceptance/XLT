package scripting.neg.deleteCookie;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class DeleteCookie_name_invalid extends AbstractWebDriverScriptTestCase
{
	/**
	 * Constructor.
	 */
	public DeleteCookie_name_invalid()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected=XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		deleteCookie( ",; ?()<>/=" );
		deleteCookie( "" );
		deleteCookie( null );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}