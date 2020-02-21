package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.waitForVisible_actions.waitForVisibleAction;

/**
 * TODO: Add class description
 */
public class waitForVisible extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public waitForVisible()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new waitForVisibleAction(lastAction);
        lastAction.run();


    }
}