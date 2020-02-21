package scripting.neg.pause;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class Pause extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Pause()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		type( "id=timeout_field", "500" );
		click("xpath=id('appear')/input[@value='appear' and @type='submit']");

		pause( 10 );
		assertText("xpath=id('appear')", "appear_2 ");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}