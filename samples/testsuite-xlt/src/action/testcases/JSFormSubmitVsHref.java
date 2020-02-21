package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction0;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction1;

/**
 * TODO: Add class description
 */
public class JSFormSubmitVsHref extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public JSFormSubmitVsHref()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction0(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage1 = new Open_ExamplePage();
        lastAction = open_ExamplePage1.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction1(lastAction);
        lastAction.run();


    }
}