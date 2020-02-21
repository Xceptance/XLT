package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.storeTitle_actions.storeTitleAction;
import action.modules.assertTitle_frame;
import action.testcases.storeTitle_actions.storeTitleAction0;

/**
 * TODO: Add class description
 */
public class storeTitle extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public storeTitle()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new storeTitleAction(lastAction);
        lastAction.run();

        final assertTitle_frame assertTitle_frame = new assertTitle_frame();
        lastAction = assertTitle_frame.run(lastAction);

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new storeTitleAction0(lastAction);
        lastAction.run();


    }
}