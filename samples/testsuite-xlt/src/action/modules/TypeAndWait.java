package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.TypeAndWait_actions.FillForm;

/**
 * TODO: Add class description
 */
public class TypeAndWait extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public TypeAndWait()
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

        lastAction = new FillForm(lastAction);
        lastAction.run();


        return lastAction;
    }
}