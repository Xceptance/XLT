package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertAttribute_actions.complete;
import action.testcases.assertAttribute_actions.implicit_glob;
import action.testcases.assertAttribute_actions.whitespaces;
import action.testcases.assertAttribute_actions.matching_strategies;
import action.testcases.assertAttribute_actions.attribute_vs_module_parameter;

/**
 * TODO: Add class description
 */
public class assertAttribute extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertAttribute()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new complete(lastAction);
        lastAction.run();

        lastAction = new implicit_glob(lastAction);
        lastAction.run();

        lastAction = new whitespaces(lastAction);
        lastAction.run();

        lastAction = new matching_strategies(lastAction);
        lastAction.run();

        lastAction = new attribute_vs_module_parameter(lastAction);
        lastAction.run();


    }
}