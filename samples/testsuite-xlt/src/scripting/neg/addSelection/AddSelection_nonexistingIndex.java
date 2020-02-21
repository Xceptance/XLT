package scripting.neg.addSelection;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class AddSelection_nonexistingIndex extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AddSelection_nonexistingIndex()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		addSelection( "id=select_18", "index=99" );
	}
	
	@After
	public void after()
	{
		getWebDriver().quit();
	}
}