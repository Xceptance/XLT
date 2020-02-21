package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.type_actions.events;
import action.modules.type_actions.input_keyspace_lower;
import action.modules.type_actions.input_keyspace_upper;
import action.modules.type_actions.input_keyspace_altgr;
import action.modules.type_actions.textarea_keypsace;
import action.modules.type_actions.input_keyspace_upper0;
import action.modules.type_actions.input_keyspace_altgr0;
import action.modules.type_actions.clear_input;
import action.modules.type_actions.emptyValueTarget;
import action.modules.type_actions.HTML5inputTypes;
import action.modules.type_actions.strange;

/**
 * TODO: Add class description
 */
public class type extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public type()
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

        lastAction = new events(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_lower(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_upper(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_altgr(lastAction);
        lastAction.run();

        lastAction = new textarea_keypsace(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_upper0(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_altgr0(lastAction);
        lastAction.run();

        lastAction = new clear_input(lastAction);
        lastAction.run();

        lastAction = new emptyValueTarget(lastAction);
        lastAction.run();

        lastAction = new HTML5inputTypes(lastAction);
        lastAction.run();

        lastAction = new strange(lastAction);
        lastAction.run();


        return lastAction;
    }
}