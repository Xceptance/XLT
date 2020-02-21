package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.assertTitle_frame_actions.OpenFramePage;
import action.modules.assertTitle_frame_actions.AssertTitleAction;

/**
 * TODO: Add class description
 */
public class assertTitle_frame extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertTitle_frame()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new OpenFramePage(lastAction, "testpages/frame.html");
        lastAction.run();

        lastAction = new AssertTitleAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}