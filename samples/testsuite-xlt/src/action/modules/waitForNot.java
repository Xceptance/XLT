package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.waitForNot_actions.waitForNotAction;
import action.modules.waitForNot_actions.WaitForNotEvalAction;
import action.modules.waitForNot_actions.WaitForNotXpathCountAction;
import action.modules.waitForNot_actions.waitForNotSelected;
import action.modules.waitForNot_actions.WaitForNotCheckedAction;
import action.modules.waitForNot_actions.WaitForNotValueAction;

/**
 * TODO: Add class description
 */
public class waitForNot extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public waitForNot()
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

        lastAction = new waitForNotAction(lastAction);
        lastAction.run();

        lastAction = new WaitForNotEvalAction(lastAction);
        lastAction.run();

        lastAction = new WaitForNotXpathCountAction(lastAction);
        lastAction.run();

        lastAction = new waitForNotSelected(lastAction);
        lastAction.run();

        lastAction = new WaitForNotCheckedAction(lastAction);
        lastAction.run();

        lastAction = new WaitForNotValueAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}