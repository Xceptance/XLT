package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertNotStyle_actions.byStyleAttribute;
import action.testcases.assertNotStyle_actions.byIdAndClass;
import action.testcases.assertNotStyle_actions.invalid;

/**
 * TODO: Add class description
 */
public class assertNotStyle extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotStyle()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new byStyleAttribute(lastAction);
        lastAction.run();

        lastAction = new byIdAndClass(lastAction);
        lastAction.run();

        lastAction = new invalid(lastAction);
        lastAction.run();


    }
}