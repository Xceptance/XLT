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
public class Check_input extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Check_input()
	{
		super( new XltDriver( true ), null);
	}

	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		check( "id=in_txt_1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}