package scripting.neg.locator.xpath;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Xpath_invalid extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Xpath_invalid()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * old-fashion xpath
	 * 
	 * @throws Throwable
	 */
	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertText( "xpath=//div[@id='xyz']", "ipsum" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}