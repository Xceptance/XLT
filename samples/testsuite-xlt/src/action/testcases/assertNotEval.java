package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertNotEval_actions.assertNotEvalAction;

/**
 * TODO: Add class description
 */
public class assertNotEval extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotEval()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new assertNotEvalAction(lastAction);
        lastAction.run();


    }
}