package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertTextPresent extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertTextPresent()
	{
		super( new XltDriver( true ), null );
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertTextPresent( "          " );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}