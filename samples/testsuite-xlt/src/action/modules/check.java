package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.check_actions.initial;
import action.modules.check_actions.CheckAction;
import action.modules.check_actions.double_check;
import action.modules.check_actions.UncheckAction;
import action.modules.check_actions.double_uncheck;
import action.modules.check_actions.sequence;
import action.modules.check_actions.radio_button0;
import action.modules.check_actions.different_selectors;
import action.modules.check_actions.different_selectors0;

/**
 * TODO: Add class description
 */
public class check extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public check()
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

        lastAction = new initial(lastAction);
        lastAction.run();

        lastAction = new CheckAction(lastAction);
        lastAction.run();

        lastAction = new double_check(lastAction);
        lastAction.run();

        lastAction = new UncheckAction(lastAction);
        lastAction.run();

        lastAction = new double_uncheck(lastAction);
        lastAction.run();

        lastAction = new sequence(lastAction);
        lastAction.run();

        lastAction = new radio_button0(lastAction);
        lastAction.run();

        lastAction = new different_selectors(lastAction);
        lastAction.run();

        lastAction = new different_selectors0(lastAction);
        lastAction.run();


        return lastAction;
    }
}