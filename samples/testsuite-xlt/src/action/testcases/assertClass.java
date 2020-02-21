package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertClass_actions.SingleClassToMatch;
import action.testcases.assertClass_actions.MultipleClassesToMatch;

/**
 * TODO: Add class description
 */
public class assertClass extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertClass()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new SingleClassToMatch(lastAction);
        lastAction.run();

        lastAction = new MultipleClassesToMatch(lastAction);
        lastAction.run();


    }
}