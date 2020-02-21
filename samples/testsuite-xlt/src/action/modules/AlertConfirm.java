package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.AlertConfirm_actions.navigate;
import action.modules.AlertConfirm_actions.alert;
import action.modules.AlertConfirm_actions.confirm_true;

/**
 * TODO: Add class description
 */
public class AlertConfirm extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public AlertConfirm()
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

        lastAction = new navigate(lastAction);
        lastAction.run();

        lastAction = new alert(lastAction);
        lastAction.run();

        lastAction = new confirm_true(lastAction);
        lastAction.run();


        return lastAction;
    }
}