package action.placeholders.overrideTestdata;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.placeholders.overrideTestdata.TOverrideTestData_actions.TOverrideTestDataAction;

/**
 * Override test data in (sub) modules that use and define the test data themself.
 */
public class TOverrideTestData extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public TOverrideTestData()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new TOverrideTestDataAction(lastAction);
        lastAction.run();


    }
}