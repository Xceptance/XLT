package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.selectFrame_actions.open_frame_page;
import action.modules.selectFrame_actions.frame_1_byIndex;
import action.modules.selectFrame_actions.frame_2_byIndex;
import action.modules.selectFrame_actions.frame_1_byDom;
import action.modules.selectFrame_actions.frame_2_byDom;

/**
 * TODO: Add class description
 */
public class selectFrame extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public selectFrame()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new open_frame_page(lastAction, "testpages/frame.html");
        lastAction.run();

        lastAction = new frame_1_byIndex(lastAction);
        lastAction.run();

        lastAction = new frame_2_byIndex(lastAction);
        lastAction.run();

        lastAction = new frame_1_byDom(lastAction);
        lastAction.run();

        lastAction = new frame_2_byDom(lastAction);
        lastAction.run();


        return lastAction;
    }
}