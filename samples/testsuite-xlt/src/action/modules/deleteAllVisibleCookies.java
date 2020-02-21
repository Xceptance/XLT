package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.deleteAllVisibleCookies_actions.createCookies;
import action.modules.deleteAllVisibleCookies_actions.checkPresence;
import action.modules.deleteAllVisibleCookies_actions.deleteAll;
import action.modules.deleteAllVisibleCookies_actions.checkAbsence;

/**
 * TODO: Add class description
 */
public class deleteAllVisibleCookies extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public deleteAllVisibleCookies()
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

        lastAction = new createCookies(lastAction);
        lastAction.run();

        lastAction = new checkPresence(lastAction);
        lastAction.run();

        lastAction = new deleteAll(lastAction);
        lastAction.run();

        lastAction = new checkAbsence(lastAction);
        lastAction.run();


        return lastAction;
    }
}