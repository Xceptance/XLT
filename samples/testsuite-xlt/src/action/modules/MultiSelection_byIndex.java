package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.MultiSelection_byIndex_actions.add_specialChars_byIndex;
import action.modules.MultiSelection_byIndex_actions.remove_specialChars_byIndex;
import action.modules.MultiSelection_byIndex_actions.double_select_byIndex;
import action.modules.MultiSelection_byIndex_actions.remove_unselected_byIndex;

/**
 * TODO: Add class description
 */
public class MultiSelection_byIndex extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public MultiSelection_byIndex()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new add_specialChars_byIndex(lastAction);
        lastAction.run();

        lastAction = new remove_specialChars_byIndex(lastAction);
        lastAction.run();

        lastAction = new double_select_byIndex(lastAction);
        lastAction.run();

        lastAction = new remove_unselected_byIndex(lastAction);
        lastAction.run();


        return lastAction;
    }
}