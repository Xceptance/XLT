package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.mouseEvent_actions.MouseOverAction;
import action.modules.mouseEvent_actions.MouseDownAction;
import action.modules.mouseEvent_actions.MouseUpAction;
import action.modules.mouseEvent_actions.MouseOutAction;
import action.modules.mouseEvent_actions.MouseOverAction0;
import action.modules.mouseEvent_actions.MouseDownAtAction;
import action.modules.mouseEvent_actions.MouseMoveAtAction;
import action.modules.mouseEvent_actions.MouseUpAtAction;
import action.modules.mouseEvent_actions.MouseMoveAction;

/**
 * TODO: Add class description
 */
public class mouseEvent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public mouseEvent()
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

        lastAction = new MouseOverAction(lastAction);
        lastAction.run();

        lastAction = new MouseDownAction(lastAction);
        lastAction.run();

        lastAction = new MouseUpAction(lastAction);
        lastAction.run();

        lastAction = new MouseOutAction(lastAction);
        lastAction.run();

        lastAction = new MouseOverAction0(lastAction);
        lastAction.run();

        lastAction = new MouseDownAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseMoveAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseUpAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseMoveAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}