package action.placeholders.injectTestdata;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.placeholders.injectTestdata.TInjectTestData_actions.TInjectTestDataAction;
import action.placeholders.injectTestdata.TInjectTestData_actions.TInjectTestData_0;

/**
 * Inject test data to module that doesn't define the test data itself (no override, just injection)
 */
public class TInjectTestData extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public TInjectTestData()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new TInjectTestDataAction(lastAction);
        lastAction.run();

        lastAction = new TInjectTestData_0(lastAction);
        lastAction.run();


    }
}