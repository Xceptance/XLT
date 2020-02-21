package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class doubleclick extends AbstractWebDriverModule
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
        // ~~~ doubleclick-button ~~~
        //
        startAction("doubleclick_button");
        doubleClick("id=doubleclick");
        assertText("id=cc_dblclick_head", "dblclick (doubleclick)");

        //
        // ~~~ doubleclick-gif ~~~
        //
        startAction("doubleclick_gif");
        doubleClick("id=doubleclick_gif");
        assertText("id=cc_dblclick_head", "dblclick (doubleclick_gif)");

    }
}