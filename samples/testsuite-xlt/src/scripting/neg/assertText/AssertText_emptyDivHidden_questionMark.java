package scripting.neg.assertText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;

/**
 * 
 */
public class AssertText_emptyDivHidden_questionMark extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public AssertText_emptyDivHidden_questionMark()
    {
        super(new XltDriver(true), null);
    }

    @Test(expected = AssertionError.class)
    public void test() throws Throwable
    {
        PageOpener.examplePage(this);
        assertText("id=invisible_empty_div", "?*");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}