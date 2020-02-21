package scripting.neg.assertNotTitle;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Title;


/**
 * 
 */
public class AssertNotTitle_emptyTitle_spaces extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotTitle_emptyTitle_spaces()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Title.empty( this );
		assertNotTitle( "     " );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}