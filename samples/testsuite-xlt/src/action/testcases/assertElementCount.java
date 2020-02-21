package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertElementCount_actions.assertCount;
import action.testcases.assertElementCount_actions.assertNotCount;
import action.testcases.assertElementCount_actions.assertNotElement;
import action.testcases.assertElementCount_actions.waitFor;
import action.testcases.assertElementCount_actions.waitForNot;

/**
 * TODO: Add class description
 */
public class assertElementCount extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertElementCount()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new assertCount(lastAction);
        lastAction.run();

        lastAction = new assertNotCount(lastAction);
        lastAction.run();

        lastAction = new assertNotElement(lastAction);
        lastAction.run();

        lastAction = new waitFor(lastAction);
        lastAction.run();

        lastAction = new waitForNot(lastAction);
        lastAction.run();


    }
}