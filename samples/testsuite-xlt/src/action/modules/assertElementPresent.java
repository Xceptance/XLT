package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertElementPresent_actions.anchor_link;
import action.modules.assertElementPresent_actions.anchor_name;
import action.modules.assertElementPresent_actions.image;
import action.modules.assertElementPresent_actions.checkbox;
import action.modules.assertElementPresent_actions.radio_button;
import action.modules.assertElementPresent_actions.submit_button;
import action.modules.assertElementPresent_actions.text_input_field;
import action.modules.assertElementPresent_actions.h1;
import action.modules.assertElementPresent_actions.not_visibile;
import action.modules.assertElementPresent_actions.not_displayed;
import action.modules.assertElementPresent_actions.hidden_input;
import action.modules.assertElementPresent_actions.empty;
import action.modules.assertElementPresent_actions.iframe1;
import action.modules.assertElementPresent_actions.iframe2;

/**
 * TODO: Add class description
 */
public class assertElementPresent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertElementPresent()
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

        lastAction = new anchor_link(lastAction);
        lastAction.run();

        lastAction = new anchor_name(lastAction);
        lastAction.run();

        lastAction = new image(lastAction);
        lastAction.run();

        lastAction = new checkbox(lastAction);
        lastAction.run();

        lastAction = new radio_button(lastAction);
        lastAction.run();

        lastAction = new submit_button(lastAction);
        lastAction.run();

        lastAction = new text_input_field(lastAction);
        lastAction.run();

        lastAction = new h1(lastAction);
        lastAction.run();

        lastAction = new not_visibile(lastAction);
        lastAction.run();

        lastAction = new not_displayed(lastAction);
        lastAction.run();

        lastAction = new hidden_input(lastAction);
        lastAction.run();

        lastAction = new empty(lastAction);
        lastAction.run();

        lastAction = new iframe1(lastAction);
        lastAction.run();

        lastAction = new iframe2(lastAction);
        lastAction.run();


        return lastAction;
    }
}