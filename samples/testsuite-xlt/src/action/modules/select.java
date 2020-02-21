package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Select_byLabel;
import action.modules.Select_byValue;
import action.modules.Select_byIndex;
import action.modules.SelectAndWait;
import action.modules.Open_ExamplePage;
import action.modules.Select_easy;
import action.modules.select_actions.multi_select;
import action.modules.select_actions.locators;

/**
 * TODO: Add class description
 */
public class select extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public select()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Select_byLabel select_byLabel = new Select_byLabel();
        lastAction = select_byLabel.run(lastAction);

        final Select_byValue select_byValue = new Select_byValue();
        lastAction = select_byValue.run(lastAction);

        final Select_byIndex select_byIndex = new Select_byIndex();
        lastAction = select_byIndex.run(lastAction);

        final SelectAndWait selectAndWait = new SelectAndWait();
        lastAction = selectAndWait.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        final Select_easy select_easy = new Select_easy("id");
        lastAction = select_easy.run(lastAction);

        lastAction = new multi_select(lastAction);
        lastAction.run();

        lastAction = new locators(lastAction);
        lastAction.run();


        return lastAction;
    }
}