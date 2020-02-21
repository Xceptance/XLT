package scripting.neg.selectFrame;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class SelectFrame_oldFashioned extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public SelectFrame_oldFashioned()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		selectFrame( "dom=frames[0]" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}