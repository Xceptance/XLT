package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.SelectAndWait_actions.SelectAndWaitAction;

/**
 * <p>Test selectAndWait command</p>
 */
public class SelectAndWait extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public SelectAndWait()
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

        lastAction = new SelectAndWaitAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}