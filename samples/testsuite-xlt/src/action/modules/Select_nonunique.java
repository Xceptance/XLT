package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Select_nonunique_actions.nonunique;
import action.modules.Select_nonunique_actions.multi_select;

/**
 * TODO: Add class description
 */
public class Select_nonunique extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public Select_nonunique(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new nonunique(lastAction, optionLocator);
        lastAction.run();

        lastAction = new multi_select(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}