package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotTitle extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        assertElementPresent("xpath=//title");

        //
        // ~~~ substring ~~~
        //
        startAction("substring");
        assertNotTitle("xample");

        //
        // ~~~ special ~~~
        //
        startAction("special");
        assertNotTitle("xyz");
        assertNotTitle("");
        assertNotTitle("exact:");
        assertNotTitle("glob:");

        //
        // ~~~ pageWithEmptyTitle ~~~
        //
        startAction("pageWithEmptyTitle");
        click("id=title_empty");
        assertElementPresent("xpath=//title");
        assertNotTitle("example page");
        assertNotTitle("regexp:.+");

        //
        // ~~~ pageWithNoTitle ~~~
        //
        startAction("pageWithNoTitle");
        click("id=title_remove");
        assertNotElementPresent("xpath=//title");
        assertNotTitle("example page");
        assertNotTitle("regexp:.+");

    }
}