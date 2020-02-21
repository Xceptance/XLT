package scripting.neg.createCookie;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class CreateCookie_name_space extends AbstractWebDriverScriptTestCase
{
	/**
	 * Constructor.
	 */
	public CreateCookie_name_space()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		createCookie( "foo bar=foofoo" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}