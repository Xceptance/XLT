package scripting.neg.selectWindow;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchWindowException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class SelectWindow_name_notExisting extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public SelectWindow_name_notExisting()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchWindowException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		selectWindow( "name=xyz" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}