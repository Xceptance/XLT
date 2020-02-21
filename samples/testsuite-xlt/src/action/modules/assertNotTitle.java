package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotTitle_actions.assertNotTitleAction;
import action.modules.assertNotTitle_actions.substring;
import action.modules.assertNotTitle_actions.special;
import action.modules.assertNotTitle_actions.pageWithEmptyTitle;
import action.modules.assertNotTitle_actions.pageWithNoTitle;

/**
 * TODO: Add class description
 */
public class assertNotTitle extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotTitle()
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

        lastAction = new assertNotTitleAction(lastAction);
        lastAction.run();

        lastAction = new substring(lastAction);
        lastAction.run();

        lastAction = new special(lastAction);
        lastAction.run();

        lastAction = new pageWithEmptyTitle(lastAction);
        lastAction.run();

        lastAction = new pageWithNoTitle(lastAction);
        lastAction.run();


        return lastAction;
    }
}