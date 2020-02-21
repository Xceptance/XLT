package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.waitFor_actions.waitForAction;
import action.modules.waitFor_actions.WaitForEvalAction;
import action.modules.waitFor_actions.WaitForXpathCountAction;
import action.modules.waitFor_actions.waitForSelected;
import action.modules.waitFor_actions.WaitForCheckedAction;
import action.modules.waitFor_actions.WaitForValueAction;
import action.modules.waitFor_actions.popup_0;
import action.modules.waitFor_actions.popup_1;
import action.modules.waitFor_actions.popup_2;

/**
 * TODO: Add class description
 */
public class waitFor extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public waitFor()
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

        lastAction = new waitForAction(lastAction);
        lastAction.run();

        lastAction = new WaitForEvalAction(lastAction);
        lastAction.run();

        lastAction = new WaitForXpathCountAction(lastAction);
        lastAction.run();

        lastAction = new waitForSelected(lastAction);
        lastAction.run();

        lastAction = new WaitForCheckedAction(lastAction);
        lastAction.run();

        lastAction = new WaitForValueAction(lastAction);
        lastAction.run();

        lastAction = new popup_0(lastAction);
        lastAction.run();

        lastAction = new popup_1(lastAction);
        lastAction.run();

        lastAction = new popup_2(lastAction);
        lastAction.run();


        return lastAction;
    }
}