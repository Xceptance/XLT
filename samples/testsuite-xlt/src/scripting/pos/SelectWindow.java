package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class SelectWindow extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public SelectWindow()
	{
		super(new XltDriver(true), null);
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		// open popup
		click( "id=popup_w2" );
		waitForPopUp( "popup_w2" );

		// now switch to popup and back

		// by NULL
		selectWindow( "name=popup_w2" );
		assertTitle( "frame parent" );
		selectWindow( null );
		assertTitle( "example page" );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}