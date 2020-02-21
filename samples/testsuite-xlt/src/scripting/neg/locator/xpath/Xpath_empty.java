package scripting.neg.locator.xpath;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * 
 */
public class Xpath_empty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Xpath_empty()
	{
		super( new XltDriver( true ), null );
	}

	/**
	 * xpath empty
	 * 
	 * @throws Throwable
	 */
	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( "", 1 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}