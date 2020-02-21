package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.MultiSelection_byLabel;
import action.modules.MultiSelection_byValue;
import action.modules.MultiSelection_byIndex;
import action.modules.Open_ExamplePage;
import action.modules.MultiSelection_easy;
import action.modules.multiSelection_actions.locators;

/**
 * TODO: Add class description
 */
public class multiSelection extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public multiSelection()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final MultiSelection_byLabel multiSelection_byLabel = new MultiSelection_byLabel();
        lastAction = multiSelection_byLabel.run(lastAction);

        final MultiSelection_byValue multiSelection_byValue = new MultiSelection_byValue();
        lastAction = multiSelection_byValue.run(lastAction);

        final MultiSelection_byIndex multiSelection_byIndex = new MultiSelection_byIndex();
        lastAction = multiSelection_byIndex.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        final MultiSelection_easy multiSelection_easy = new MultiSelection_easy("id");
        lastAction = multiSelection_easy.run(lastAction);

        lastAction = new locators(lastAction);
        lastAction.run();


        return lastAction;
    }
}