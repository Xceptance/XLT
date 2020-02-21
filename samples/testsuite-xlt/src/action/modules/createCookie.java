package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.createCookie_actions.cleanup;
import action.modules.createCookie_actions.create;
import action.modules.createCookie_actions.overwrite;
import action.modules.createCookie_actions.empty_cookie_value;
import action.modules.createCookie_actions.optionsString;
import action.modules.createCookie_actions.quotedString;
import action.modules.createCookie_actions.create_without_open_page;
import action.modules.createCookie_actions.createCookieAction;
import action.modules.createCookie_actions.cleanup0;

/**
 * TODO: Add class description
 */
public class createCookie extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public createCookie()
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

        lastAction = new cleanup(lastAction);
        lastAction.run();

        lastAction = new create(lastAction);
        lastAction.run();

        lastAction = new overwrite(lastAction);
        lastAction.run();

        lastAction = new empty_cookie_value(lastAction);
        lastAction.run();

        lastAction = new optionsString(lastAction);
        lastAction.run();

        lastAction = new quotedString(lastAction);
        lastAction.run();

        lastAction = new create_without_open_page(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new createCookieAction(lastAction);
        lastAction.run();

        lastAction = new cleanup0(lastAction);
        lastAction.run();


        return lastAction;
    }
}