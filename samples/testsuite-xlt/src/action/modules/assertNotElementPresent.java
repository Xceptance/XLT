package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotElementPresent_actions.nonexisting;
import action.modules.assertNotElementPresent_actions.nonexisting_in_iframe1;
import action.modules.assertNotElementPresent_actions.nonexisting_in_iframe2;

/**
 * TODO: Add class description
 */
public class assertNotElementPresent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotElementPresent()
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

        lastAction = new nonexisting(lastAction);
        lastAction.run();

        lastAction = new nonexisting_in_iframe1(lastAction);
        lastAction.run();

        lastAction = new nonexisting_in_iframe2(lastAction);
        lastAction.run();


        return lastAction;
    }
}