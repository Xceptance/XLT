package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.TypeAndWait;

/**
 * TODO: Add class description
 */
public class type extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public type()
    {
        super(new XltDriver(true), "http://localhost:8080/");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final scripting.modules.type _type = new scripting.modules.type();
        _type.execute();

        final TypeAndWait _typeAndWait = new TypeAndWait();
        _typeAndWait.execute();


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