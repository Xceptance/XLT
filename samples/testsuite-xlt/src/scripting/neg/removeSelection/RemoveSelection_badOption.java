package scripting.neg.removeSelection;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class RemoveSelection_badOption extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public RemoveSelection_badOption()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		removeSelection( "id=select_18", "xyz=1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}