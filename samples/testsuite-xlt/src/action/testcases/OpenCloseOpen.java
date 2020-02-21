package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction;
import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction0;
import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction1;

/**
 * Related to #1728
 Close the last open window/tab and open new page.
 */
public class OpenCloseOpen extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public OpenCloseOpen()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        lastAction = new OpenCloseOpenAction(lastAction, "/testpages/examplePage_1.html");
        lastAction.run();

        lastAction = new OpenCloseOpenAction0(lastAction);
        lastAction.run();

        lastAction = new OpenCloseOpenAction1(lastAction, "/testpages/examplePage_1.html");
        lastAction.run();


    }
}