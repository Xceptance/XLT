package scripting.neg.assertNotText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Visibility;


/**
 * 
 */
public class AssertNotText_emptyDiv_asterisc extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_emptyDiv_asterisc()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Visibility.showEmptyDiv( this );
		assertNotText( "id=invisible_empty_div", "*" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}