package scripting.neg.assertNotXpathCount;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidSelectorException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class AssertNotXpathCount_locator_dom extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotXpathCount_locator_dom()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * located by dom
	 * 
	 * @throws Throwable
	 */
	@Test(expected = InvalidSelectorException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		assertNotXpathCount( "dom=document.getElementById('anc_sel1')", 0 );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}