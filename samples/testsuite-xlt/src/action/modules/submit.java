package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.submit_actions.submitAction;
import action.modules.submit_actions.submitAction0;
import action.modules.submit_actions.submitAction1;
import action.modules.submit_actions.submitAction2;
import action.modules.submit_actions.submitAction3;
import action.modules.submit_actions.submitAction4;

/**
 * TODO: Add class description
 */
public class submit extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public submit()
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

        lastAction = new submitAction(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new submitAction0(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage1 = new Open_ExamplePage();
        lastAction = open_ExamplePage1.run(lastAction);

        lastAction = new submitAction1(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage2 = new Open_ExamplePage();
        lastAction = open_ExamplePage2.run(lastAction);

        lastAction = new submitAction2(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage3 = new Open_ExamplePage();
        lastAction = open_ExamplePage3.run(lastAction);

        lastAction = new submitAction3(lastAction);
        lastAction.run();

        //final Open_ExamplePage open_ExamplePage4 = new Open_ExamplePage();
        //lastAction = open_ExamplePage4.run(lastAction);
        lastAction = new submitAction4(lastAction);
        lastAction.run();


        return lastAction;
    }
}