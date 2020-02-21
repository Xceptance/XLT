package scripting.neg.type;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Type_locator_index extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Type_locator_index()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		type( "index=0", "12345" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}