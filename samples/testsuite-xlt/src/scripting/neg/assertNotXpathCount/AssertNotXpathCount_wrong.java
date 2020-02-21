package scripting.neg.assertNotXpathCount;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertNotXpathCount_wrong extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotXpathCount_wrong()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * path is wrong
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotXpathCount( "id(\"xpath_wrong\")", 0 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}