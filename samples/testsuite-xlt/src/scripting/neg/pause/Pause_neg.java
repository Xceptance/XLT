package scripting.neg.pause;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Pause_neg extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Pause_neg()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		type( "id=timeout_field", "500" );
		click("xpath=id('appear')/input[@value='appear' and @type='submit']");

		pause( -1000 );
		assertText("xpath=id('appear')", "appear_2 ");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}