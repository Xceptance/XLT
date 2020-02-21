package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Open_popup_w2;
import scripting.modules.SelectWindow_popup_w2;

/**
 * TODO: Add class description
 */
public class close extends AbstractWebDriverModule
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
        // ~~~ popup ~~~
        //
        startAction("popup");
        final Open_popup_w2 _open_popup_w2 = new Open_popup_w2();
        _open_popup_w2.execute();

        final SelectWindow_popup_w2 _selectWindow_popup_w2 = new SelectWindow_popup_w2();
        _selectWindow_popup_w2.execute();

        close();

        //
        // ~~~ clean_up ~~~
        //
        startAction("clean_up");
        // necessary to get back focus on main window
        selectWindow("title=example page");

    }
}