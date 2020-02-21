package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Select_easy_actions.letters_only;
import action.modules.Select_easy_actions.with_whitespace;
import action.modules.Select_easy_actions.DoubleAction;

/**
 * TODO: Add class description
 */
public class Select_easy extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public Select_easy(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new letters_only(lastAction, optionLocator);
        lastAction.run();

        lastAction = new with_whitespace(lastAction, optionLocator);
        lastAction.run();

        lastAction = new DoubleAction(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}