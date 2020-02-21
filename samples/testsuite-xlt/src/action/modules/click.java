package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.click_actions.anchor;
import action.modules.click_actions.submit_button0;
import action.modules.click_actions.image0;
import action.modules.click_actions.div;
import action.modules.click_actions.whitespace_in_link_name;
import action.modules.click_actions.whitespace_in_linked_file_name;

/**
 * TODO: Add class description
 */
public class click extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public click()
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

        lastAction = new anchor(lastAction);
        lastAction.run();

        lastAction = new submit_button0(lastAction);
        lastAction.run();

        lastAction = new image0(lastAction);
        lastAction.run();

        lastAction = new div(lastAction);
        lastAction.run();

        lastAction = new whitespace_in_link_name(lastAction);
        lastAction.run();

        lastAction = new whitespace_in_linked_file_name(lastAction);
        lastAction.run();


        return lastAction;
    }
}