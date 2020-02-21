package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.storeEval_actions.storeEvalAction;

/**
 * TODO: Add class description
 */
public class storeEval extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public storeEval()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new storeEvalAction(lastAction);
        lastAction.run();


    }
}