package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertTitle extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertTitle()
	{
		super( new XltDriver( true ), null );
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		click( "id=title_empty" );
		assertTitle( "     " );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}