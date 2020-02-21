package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertXpathCount_actions.existing_correctCount;
import action.modules.assertXpathCount_actions.iframe4;

/**
 * TODO: Add class description
 */
public class assertXpathCount extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertXpathCount()
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

        lastAction = new existing_correctCount(lastAction);
        lastAction.run();

        lastAction = new iframe4(lastAction);
        lastAction.run();


        return lastAction;
    }
}