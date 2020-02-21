package scripting.neg.check;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Check_locator_link extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Check_locator_link()
	{
		super( new XltDriver( true ), null);
	}

	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		check( "link=anc_sel1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}