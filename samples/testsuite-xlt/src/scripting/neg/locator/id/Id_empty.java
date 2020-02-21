package scripting.neg.locator.id;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class Id_empty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Id_empty()
	{
		super( new XltDriver(true), null );
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertText( "id=", "text" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}