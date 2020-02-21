package scripting.neg.assertNotTextPresent;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.scripting.ScriptException;
import scripting.util.PageOpener;


/**
 * 
 */
@Ignore
public class AssertNotTextPresent_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotTextPresent_null()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = ScriptException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotTextPresent( null );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}