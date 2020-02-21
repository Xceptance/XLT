package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class AlertConfirm extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();


        //
        // ~~~ navigate ~~~
        //
        startAction("navigate");
        click("link=popup");

        //
        // ~~~ alert ~~~
        //
        startAction("alert");
        click("id=popup_alert");
        assertText("id=cc_misc_head", "misc (popup_alert)");

        //
        // ~~~ confirm-true ~~~
        //
        startAction("confirm_true");
        click("id=popup_confirm");
        assertText("id=cc_misc", "misc (popup_confirm) true");

    }
}
