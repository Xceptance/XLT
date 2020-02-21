package scripting.neg.assertText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Visibility;


/**
 * 
 */
public class AssertText_emptyDiv_questionMark extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText_emptyDiv_questionMark()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Visibility.showEmptyDiv( this );
		assertText( "id=invisible_empty_div", "?*" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}