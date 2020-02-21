package scripting.neg.assertTitle;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Title;


/**
 * 
 */
public class AssertTitle_emptyTitle_regexp_anyChar extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertTitle_emptyTitle_regexp_anyChar()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Title.empty( this );
		assertTitle( "regexp:.+" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}