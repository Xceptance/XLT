package scripting.neg.submit;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Submit_locator_linkInside extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Submit_locator_linkInside()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		submit( "link=form1_a1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}