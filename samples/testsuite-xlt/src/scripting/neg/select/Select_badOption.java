package scripting.neg.select;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Select_badOption extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Select_badOption()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		select( "id=select_1", "xyz=1" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}