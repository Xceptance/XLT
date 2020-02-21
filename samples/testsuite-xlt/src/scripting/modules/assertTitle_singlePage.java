package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertTitle_singlePage extends AbstractWebDriverModule
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
        // ~~~ existingTitle ~~~
        //
        startAction("existingTitle");
        assertTitle("exact:example page");
        assertTitle("example page");
        assertTitle("glob:example page");
        assertTitle("*ple pag*");

        //
        // ~~~ emptyTitle ~~~
        //
        startAction("emptyTitle");
        click("id=title_empty");
        assertTitle("");
        assertTitle("       ");
        assertTitle("glob:");
        assertTitle("exact:");

        //
        // ~~~ noTitle ~~~
        //
        startAction("noTitle");
        click("id=title_remove");
        assertTitle("");
        assertTitle("       ");
        assertTitle("glob:");
        assertTitle("exact:");

    }
}