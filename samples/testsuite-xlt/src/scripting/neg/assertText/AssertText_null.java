package scripting.neg.assertText;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
@Ignore
public class AssertText_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText_null()
	{
		super(new XltDriver(true), null);
	}

	@Test//(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertText( "id=page_headline", null );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}