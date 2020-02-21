package scripting.neg.locator.id;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class id_nonexisting extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public id_nonexisting()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		/*
		 * non existing ID
		 */
		assertText( "id=xyz", "Example Page" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}