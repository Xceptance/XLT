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
public class SelectWindow_title_notExisting extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public SelectWindow_title_notExisting()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchWindowException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		selectWindow( "title=xyz" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}