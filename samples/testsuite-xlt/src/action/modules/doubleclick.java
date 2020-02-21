package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.doubleclick_actions.doubleclick_button;
import action.modules.doubleclick_actions.doubleclick_gif;

/**
 * TODO: Add class description
 */
public class doubleclick extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public doubleclick()
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

        lastAction = new doubleclick_button(lastAction);
        lastAction.run();

        lastAction = new doubleclick_gif(lastAction);
        lastAction.run();


        return lastAction;
    }
}