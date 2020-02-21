package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class pause extends AbstractWebDriverModule
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
        // ~~~ setTimeout ~~~
        //
        startAction("setTimeout");
        type("id=timeout_field", "1000");
        click("xpath=id('appear')/input[@value='appear' and @type='submit']");
        pause(3000);
        assertText("xpath=id('appear')", "*appear_2*");
        pause(0);
        assertText("id=disapp_1", "glob:disapp_1 : div tag with ID");

    }
}