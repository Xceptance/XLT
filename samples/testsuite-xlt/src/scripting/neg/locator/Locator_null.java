package scripting.neg.locator;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
@Ignore
public class Locator_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Locator_null()
	{
		super(new XltDriver(true), null);
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertText( null, "Example Page" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}