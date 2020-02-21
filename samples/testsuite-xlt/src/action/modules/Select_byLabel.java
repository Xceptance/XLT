package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.Select_easy;
import action.modules.Select_matching;
import action.modules.Select_nonunique;
import action.modules.Select_specialChar;
import action.modules.Select_byLabel_actions.Select_byLabelAction;
import action.modules.Select_byLabel_actions.multi_select;

/**
 * TODO: Add class description
 */
public class Select_byLabel extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public Select_byLabel()
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

        final Select_easy select_easy = new Select_easy("label");
        lastAction = select_easy.run(lastAction);

        final Select_matching select_matching = new Select_matching("label");
        lastAction = select_matching.run(lastAction);

        final Select_nonunique select_nonunique = new Select_nonunique("label");
        lastAction = select_nonunique.run(lastAction);

        final Select_specialChar select_specialChar = new Select_specialChar("label");
        lastAction = select_specialChar.run(lastAction);

        lastAction = new Select_byLabelAction(lastAction);
        lastAction.run();

        lastAction = new multi_select(lastAction);
        lastAction.run();


        return lastAction;
    }
}