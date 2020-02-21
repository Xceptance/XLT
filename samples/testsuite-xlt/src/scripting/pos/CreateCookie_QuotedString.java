package scripting.pos;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.AssertCookie;
import scripting.modules.Open_ExamplePage;


/**
 * 
 */
public class CreateCookie_QuotedString extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public CreateCookie_QuotedString()
    {
        super(new XltDriver(true), "http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
    	final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        deleteCookie("x_qs");
        createCookie("x_qs=\"quoted string\"");
        final AssertCookie _assertCookie = new AssertCookie();
        _assertCookie.execute("x_qs", "\"quoted string\"");

    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}