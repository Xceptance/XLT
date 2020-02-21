package scripting.neg.removeSelection;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
@Ignore
public class RemoveSelection_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public RemoveSelection_null()
	{
		super(new XltDriver(true), null);
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		removeSelection( "id=select_18", null );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}