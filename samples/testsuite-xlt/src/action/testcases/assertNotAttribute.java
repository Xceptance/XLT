package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertNotAttribute_actions.assertNotAttributeAction;

/**
 * TODO: Add class description
 */
public class assertNotAttribute extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotAttribute()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new assertNotAttributeAction(lastAction);
        lastAction.run();


    }
}