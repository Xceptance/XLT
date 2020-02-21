package scripting.neg.locator.xpath;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
@Ignore
public class Xpath_null extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Xpath_null()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * xpath null
	 * 
	 * @throws Throwable
	 */
	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( null, 1 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}