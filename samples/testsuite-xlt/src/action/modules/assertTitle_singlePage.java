package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertTitle_singlePage_actions.existingTitle;
import action.modules.assertTitle_singlePage_actions.emptyTitle;
import action.modules.assertTitle_singlePage_actions.noTitle;

/**
 * TODO: Add class description
 */
public class assertTitle_singlePage extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertTitle_singlePage()
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

        lastAction = new existingTitle(lastAction);
        lastAction.run();

        lastAction = new emptyTitle(lastAction);
        lastAction.run();

        lastAction = new noTitle(lastAction);
        lastAction.run();


        return lastAction;
    }
}