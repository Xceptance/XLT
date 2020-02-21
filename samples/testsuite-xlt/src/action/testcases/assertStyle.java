package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertStyle_actions.byStyleAttribute0;
import action.testcases.assertStyle_actions.byIdAndClass0;

/**
 * TODO: Add class description
 */
public class assertStyle extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertStyle()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new byStyleAttribute0(lastAction);
        lastAction.run();

        lastAction = new byIdAndClass0(lastAction);
        lastAction.run();


    }
}