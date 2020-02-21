package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.deleteCookie_actions.cleanup1;
import action.modules.deleteCookie_actions.delete;
import action.modules.deleteCookie_actions.delete_twice;
import action.modules.deleteCookie_actions.delete_non_existing;
import action.modules.deleteCookie_actions.delete_without_open_page;
import action.modules.deleteCookie_actions.deleteCookieAction;
import action.modules.deleteCookie_actions.cleanup2;

/**
 * TODO: Add class description
 */
public class deleteCookie extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public deleteCookie()
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

        lastAction = new cleanup1(lastAction);
        lastAction.run();

        lastAction = new delete(lastAction);
        lastAction.run();

        lastAction = new delete_twice(lastAction);
        lastAction.run();

        lastAction = new delete_non_existing(lastAction);
        lastAction.run();

        lastAction = new delete_without_open_page(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new deleteCookieAction(lastAction);
        lastAction.run();

        lastAction = new cleanup2(lastAction);
        lastAction.run();


        return lastAction;
    }
}