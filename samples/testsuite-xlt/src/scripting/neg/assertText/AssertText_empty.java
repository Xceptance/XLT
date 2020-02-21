package scripting.neg.assertText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertText_empty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText_empty()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		/*
		 * empty
		 */
		assertText( "id=specialchar_1", "" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}