package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotText_actions.checkElementPresence;
import action.modules.assertNotText_actions.non_existing;
import action.modules.assertNotText_actions.case_insensitive;
import action.modules.assertNotText_actions.existing_but_not_in_this_id;
import action.modules.assertNotText_actions.textfield;
import action.modules.assertNotText_actions.matching_strategy;
import action.modules.assertNotText_actions.emptyDiv;
import action.modules.assertNotText_actions.emptyDiv_visible;
import action.modules.assertNotText_actions.invisibleDiv;
import action.modules.assertNotText_actions.locator;
import action.modules.assertNotText_actions.iframe;
import action.modules.assertNotText_actions.subframe;

/**
 * TODO: Add class description
 */
public class assertNotText extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotText()
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

        lastAction = new checkElementPresence(lastAction);
        lastAction.run();

        lastAction = new non_existing(lastAction);
        lastAction.run();

        lastAction = new case_insensitive(lastAction);
        lastAction.run();

        lastAction = new existing_but_not_in_this_id(lastAction);
        lastAction.run();

        lastAction = new textfield(lastAction);
        lastAction.run();

        lastAction = new matching_strategy(lastAction);
        lastAction.run();

        lastAction = new emptyDiv(lastAction);
        lastAction.run();

        lastAction = new emptyDiv_visible(lastAction);
        lastAction.run();

        lastAction = new invisibleDiv(lastAction);
        lastAction.run();

        lastAction = new locator(lastAction);
        lastAction.run();

        lastAction = new iframe(lastAction);
        lastAction.run();

        lastAction = new subframe(lastAction);
        lastAction.run();


        return lastAction;
    }
}