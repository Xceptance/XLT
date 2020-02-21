package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.VisibleElementFinder_Anchor;
import action.modules.Open_ExamplePage;
import action.modules.VisibleElementFinder_actions.CheckAction0;
import action.modules.VisibleElementFinder_actions.UncheckAction0;
import action.modules.VisibleElementFinder_actions.TypeAction;
import action.modules.VisibleElementFinder_actions.SelectAction;
import action.modules.VisibleElementFinder_actions.RemoveSelectionAction;
import action.modules.VisibleElementFinder_actions.SelectAction0;

/**
 * TODO: Add class description
 */
public class VisibleElementFinder extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public VisibleElementFinder()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final VisibleElementFinder_Anchor visibleElementFinder_Anchor = new VisibleElementFinder_Anchor("name=in_visible_anchor", "iframe 1");
        lastAction = visibleElementFinder_Anchor.run(lastAction);

        final VisibleElementFinder_Anchor visibleElementFinder_Anchor0 = new VisibleElementFinder_Anchor("link=in_visible_anchor", "iframe 2");
        lastAction = visibleElementFinder_Anchor0.run(lastAction);

        final VisibleElementFinder_Anchor visibleElementFinder_Anchor1 = new VisibleElementFinder_Anchor("xpath=id('in_visible_anchor')/div/a", "iframe 1");
        lastAction = visibleElementFinder_Anchor1.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new CheckAction0(lastAction);
        lastAction.run();

        lastAction = new UncheckAction0(lastAction);
        lastAction.run();

        lastAction = new TypeAction(lastAction);
        lastAction.run();

        lastAction = new SelectAction(lastAction);
        lastAction.run();

        lastAction = new RemoveSelectionAction(lastAction);
        lastAction.run();

        lastAction = new SelectAction0(lastAction);
        lastAction.run();


        return lastAction;
    }
}