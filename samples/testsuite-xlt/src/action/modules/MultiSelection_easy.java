package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.MultiSelection_easy_actions.lettersOnly_add;
import action.modules.MultiSelection_easy_actions.lettersOnly_remove;
import action.modules.MultiSelection_easy_actions.withWhitespace_add;
import action.modules.MultiSelection_easy_actions.withWhitespace_remove;
import action.modules.MultiSelection_easy_actions.doubleSelect;
import action.modules.MultiSelection_easy_actions.doubleSelect_cleanup;
import action.modules.MultiSelection_easy_actions.removeUnselected;

/**
 * TODO: Add class description
 */
public class MultiSelection_easy extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public MultiSelection_easy(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new lettersOnly_add(lastAction, optionLocator);
        lastAction.run();

        lastAction = new lettersOnly_remove(lastAction, optionLocator);
        lastAction.run();

        lastAction = new withWhitespace_add(lastAction, optionLocator);
        lastAction.run();

        lastAction = new withWhitespace_remove(lastAction, optionLocator);
        lastAction.run();

        lastAction = new doubleSelect(lastAction, optionLocator);
        lastAction.run();

        lastAction = new doubleSelect_cleanup(lastAction, optionLocator);
        lastAction.run();

        lastAction = new removeUnselected(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}