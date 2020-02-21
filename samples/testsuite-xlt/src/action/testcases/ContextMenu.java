package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.ContextMenu_actions.ContextMenuAction;

/**
 * TODO: Add class description
 */
public class ContextMenu extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public ContextMenu()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new ContextMenuAction(lastAction);
        lastAction.run();


    }
}