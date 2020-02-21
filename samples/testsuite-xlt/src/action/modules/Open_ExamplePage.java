package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage_actions.Open_ExamplePageAction;

/**
 * TODO: Add class description
 */
public class Open_ExamplePage extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public Open_ExamplePage()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new Open_ExamplePageAction(lastAction, "testpages/examplePage_1.html");
        lastAction.run();


        return lastAction;
    }
}