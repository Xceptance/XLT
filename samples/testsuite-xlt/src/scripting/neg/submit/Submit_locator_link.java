package scripting.neg.submit;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Submit_locator_link extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Submit_locator_link()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		submit( "link=form4" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}