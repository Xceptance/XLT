package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.MultiSelection_specialChars_actions.add_special_chars_multiple;
import action.modules.MultiSelection_specialChars_actions.remove_special_chars_multiple;

/**
 * TODO: Add class description
 */
public class MultiSelection_specialChars extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public MultiSelection_specialChars(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new add_special_chars_multiple(lastAction, optionLocator);
        lastAction.run();

        lastAction = new remove_special_chars_multiple(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}