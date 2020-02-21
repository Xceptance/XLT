package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.close_actions.popup1;
import action.modules.close_actions.clean_up;

/**
 * TODO: Add class description
 */
public class close extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public close()
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

        lastAction = new popup1(lastAction);
        lastAction.run();

        lastAction = new clean_up(lastAction);
        lastAction.run();


        return lastAction;
    }
}