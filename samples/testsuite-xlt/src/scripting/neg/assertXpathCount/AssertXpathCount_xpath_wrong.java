package scripting.neg.assertXpathCount;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertXpathCount_xpath_wrong extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_xpath_wrong()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * non existing xpath
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( "xyz", 1 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}