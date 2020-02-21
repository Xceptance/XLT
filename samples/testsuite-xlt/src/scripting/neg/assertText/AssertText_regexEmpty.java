package scripting.neg.assertText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertText_regexEmpty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText_regexEmpty()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		/*
		 * number of chars after 'ipsum' is 1 instead of 3
		 */
		assertText( "id=specialchar_1", "regexp:Lorem ipsum [XYZ]{1} dolor sit amet, consectetuer adipiscing elit." );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}