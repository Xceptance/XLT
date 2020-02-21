package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.MultiSelection_easy;
import action.modules.MultiSelection_byValue_actions.MultiSelection_byValueAction;
import action.modules.MultiSelection_specialChars;
import action.modules.MultiSelection_byValue_actions.MultiSelection_byValueAction0;
import action.modules.MultiSelection_byValue_actions.label_whitespace0;

/**
 * TODO: Add class description
 */
public class MultiSelection_byValue extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public MultiSelection_byValue()
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

        final MultiSelection_easy multiSelection_easy = new MultiSelection_easy("value");
        lastAction = multiSelection_easy.run(lastAction);

        lastAction = new MultiSelection_byValueAction(lastAction);
        lastAction.run();

        final MultiSelection_specialChars multiSelection_specialChars = new MultiSelection_specialChars("value");
        lastAction = multiSelection_specialChars.run(lastAction);

        lastAction = new MultiSelection_byValueAction0(lastAction);
        lastAction.run();

        lastAction = new label_whitespace0(lastAction);
        lastAction.run();


        return lastAction;
    }
}