package scripting.neg.removeSelection;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class RemoveSelection_negativeIndex extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public RemoveSelection_negativeIndex()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		removeSelection( "id=select_18", "index=-1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}