package scripting.neg.assertNotText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Visibility;


/**
 * 
 */
public class AssertNotText_spacesOnly extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_spacesOnly()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * text exists
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Visibility.showSpacesDiv( this );
		assertNotText( "id=ws2_spaces_only", "          " );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}