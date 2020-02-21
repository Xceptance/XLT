package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotXpathCount_actions.existing_wrongCount;
import action.modules.assertNotXpathCount_actions.non_existing_element;
import action.modules.assertNotXpathCount_actions.iframe3;

/**
 * TODO: Add class description
 */
public class assertNotXpathCount extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotXpathCount()
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

        lastAction = new existing_wrongCount(lastAction);
        lastAction.run();

        lastAction = new non_existing_element(lastAction);
        lastAction.run();

        lastAction = new iframe3(lastAction);
        lastAction.run();


        return lastAction;
    }
}