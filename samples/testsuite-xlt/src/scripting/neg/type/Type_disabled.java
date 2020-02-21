package scripting.neg.type;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.InvalidElementStateException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class Type_disabled extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Type_disabled()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = InvalidElementStateException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		type( "id=in_txt_12", "write to disabled" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}