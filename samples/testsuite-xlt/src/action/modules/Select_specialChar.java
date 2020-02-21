package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Select_specialChar_actions.special_chars;

/**
 * TODO: Add class description
 */
public class Select_specialChar extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public Select_specialChar(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new special_chars(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}