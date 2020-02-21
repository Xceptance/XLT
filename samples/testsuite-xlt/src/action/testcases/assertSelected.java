package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertSelected_actions.initial_unselected;
import action.testcases.assertSelected_actions.initial_preselected;
import action.testcases.assertSelected_actions.unselect_preselected;
import action.testcases.assertSelected_actions.SelectAction;
import action.testcases.assertSelected_actions.noValueOption;
import action.testcases.assertSelected_actions.unselect_selected;

/**
 * TODO: Add class description
 */
public class assertSelected extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertSelected()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new initial_unselected(lastAction);
        lastAction.run();

        lastAction = new initial_preselected(lastAction);
        lastAction.run();

        lastAction = new unselect_preselected(lastAction);
        lastAction.run();

        lastAction = new SelectAction(lastAction);
        lastAction.run();

        lastAction = new noValueOption(lastAction);
        lastAction.run();

        lastAction = new unselect_selected(lastAction);
        lastAction.run();


    }
}