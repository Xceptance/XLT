package scripting.pos;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

public class IsChecked extends AbstractWebDriverScriptTestCase
{
    /**
     * Constructor.
     */
    public IsChecked()
    {
        super( new XltDriver( true ), null );
    }

    @Test
    public void test() throws Throwable
    {
        PageOpener.examplePage( this );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
