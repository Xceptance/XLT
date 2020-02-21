package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotTextPresent_actions.non_existing0;
import action.modules.assertNotTextPresent_actions.case_insensitive0;
import action.modules.assertNotTextPresent_actions.iframe0;
import action.modules.assertNotTextPresent_actions.subframe0;

/**
 * TODO: Add class description
 */
public class assertNotTextPresent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotTextPresent()
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

        lastAction = new non_existing0(lastAction);
        lastAction.run();

        lastAction = new case_insensitive0(lastAction);
        lastAction.run();

        lastAction = new iframe0(lastAction);
        lastAction.run();

        lastAction = new subframe0(lastAction);
        lastAction.run();


        return lastAction;
    }
}