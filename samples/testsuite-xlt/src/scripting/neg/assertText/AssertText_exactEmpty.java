package scripting.neg.assertText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertText_exactEmpty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText_exactEmpty()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		/*
		 * text is: Lorem ipsum Y dolor sit amet, consectetuer adipiscing elit.
		 */
		assertText( "id=specialchar_3", "exact:Lorem ipsum ? dolor sit amet, consectetuer adipiscing elit." );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}