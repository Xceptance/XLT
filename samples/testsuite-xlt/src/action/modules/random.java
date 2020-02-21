package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.random_actions.random_alphanumeric_string;
import action.modules.random_actions.random_number;
import action.modules.random_actions.timestamp;
import action.modules.random_actions.DoubleAction;
import action.modules.random_actions.randomParam;
import action.modules.random_actions.randomParamWithPlaceholder;

/**
 * TODO: Add class description
 */
public class random extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public random()
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

        lastAction = new random_alphanumeric_string(lastAction);
        lastAction.run();

        lastAction = new random_number(lastAction);
        lastAction.run();

        lastAction = new timestamp(lastAction);
        lastAction.run();

        lastAction = new DoubleAction(lastAction);
        lastAction.run();

        lastAction = new randomParam(lastAction);
        lastAction.run();

        lastAction = new randomParamWithPlaceholder(lastAction);
        lastAction.run();


        return lastAction;
    }
}