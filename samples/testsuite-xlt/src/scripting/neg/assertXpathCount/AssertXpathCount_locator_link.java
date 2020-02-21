package scripting.neg.assertXpathCount;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class AssertXpathCount_locator_link extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertXpathCount_locator_link()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * located by link
	 * 
	 * @throws Throwable
	 */
	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertXpathCount( "link=anc_sel1", 1 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}