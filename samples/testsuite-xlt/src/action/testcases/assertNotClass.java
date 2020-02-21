package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertNotClass_actions.SingleClassToMatch0;
import action.testcases.assertNotClass_actions.MultipleClassesToMatch0;

/**
 * TODO: Add class description
 */
public class assertNotClass extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotClass()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new SingleClassToMatch0(lastAction);
        lastAction.run();

        lastAction = new MultipleClassesToMatch0(lastAction);
        lastAction.run();


    }
}