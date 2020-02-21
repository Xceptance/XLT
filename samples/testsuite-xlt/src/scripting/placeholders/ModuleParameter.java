package scripting.placeholders;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.modules.SimpleMod_AssertText;

/**
 * TODO: Add class description
 */
public class ModuleParameter extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public ModuleParameter()
    {
        super(new XltDriver(true), "http://localhost:8080");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final SimpleMod_AssertText _simpleMod_AssertText = new SimpleMod_AssertText();
        _simpleMod_AssertText.execute("specialchar_15", "Lorem ipsum\\");

        _simpleMod_AssertText.execute("specialchar_15", "regexp:Lorem\\sipsum.*");


    }


    /**
     * Clean up.
     */
    @After
    public void after()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }
}