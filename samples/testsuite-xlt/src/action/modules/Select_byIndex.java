package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.Select_byIndex_actions.Select_byIndexAction;

/**
 * TODO: Add class description
 */
public class Select_byIndex extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public Select_byIndex()
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

        lastAction = new Select_byIndexAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}