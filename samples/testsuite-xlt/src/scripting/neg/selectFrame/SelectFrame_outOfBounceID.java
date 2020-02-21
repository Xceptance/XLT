package scripting.neg.selectFrame;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.NoSuchFrameException;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class SelectFrame_outOfBounceID extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public SelectFrame_outOfBounceID()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = NoSuchFrameException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		selectFrame( "index=99" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}