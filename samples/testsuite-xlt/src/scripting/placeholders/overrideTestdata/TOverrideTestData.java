package scripting.placeholders.overrideTestdata;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.placeholders.overrideTestdata.Mod_2c;
import scripting.placeholders.overrideTestdata.Mod_2b;
import scripting.placeholders.overrideTestdata.Mod_2a;
import scripting.placeholders.overrideTestdata.Mod_3;

/**
 * Override test data in (sub) modules that use and define the test data themself.
 */
public class TOverrideTestData extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TOverrideTestData()
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

        assertText("id=specialchar_1", resolve("${gtd1}"));
        // reset input for further testing
        type("id=in_txt_1", resolve("${t1} - 0"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 0");
        final Mod_2c _mod_2c = new Mod_2c();
        _mod_2c.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        final Mod_2b _mod_2b = new Mod_2b();
        _mod_2b.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        final Mod_2a _mod_2a = new Mod_2a();
        _mod_2a.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 1");
        final Mod_3 _mod_3 = new Mod_3();
        _mod_3.execute();


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