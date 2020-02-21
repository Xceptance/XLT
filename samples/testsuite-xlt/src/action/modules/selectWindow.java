package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.selectWindow_actions.selectWindowAction;
import action.modules.selectWindow_actions.select_popup;
import action.modules.selectWindow_actions.toggle_title;
import action.modules.selectWindow_actions.toggle_null;
import action.modules.selectWindow_actions.toggle_emptyName;
import action.modules.selectWindow_actions.close_w2;
import action.modules.selectWindow_actions.open_popup_w4;
import action.modules.selectWindow_actions.toggle_emptyTitle;
import action.modules.selectWindow_actions.close_w4;
import action.modules.selectWindow_actions.clean_up0;

/**
 * TODO: Add class description
 */
public class selectWindow extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public selectWindow()
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

        lastAction = new selectWindowAction(lastAction);
        lastAction.run();

        lastAction = new select_popup(lastAction);
        lastAction.run();

        lastAction = new toggle_title(lastAction);
        lastAction.run();

        lastAction = new toggle_null(lastAction);
        lastAction.run();

        lastAction = new toggle_emptyName(lastAction);
        lastAction.run();

        lastAction = new close_w2(lastAction);
        lastAction.run();

        lastAction = new open_popup_w4(lastAction);
        lastAction.run();

        lastAction = new toggle_emptyTitle(lastAction);
        lastAction.run();

        lastAction = new close_w4(lastAction);
        lastAction.run();

        lastAction = new clean_up0(lastAction);
        lastAction.run();


        return lastAction;
    }
}