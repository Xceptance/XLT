package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class Select_byIndex extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        //
        // ~~~ special_chars ~~~
        //
        startAction("special_chars");
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        // space
        select("id=select_17", "index=1");
        assertText("id=cc_change", "change (select_17) empty");
        // space
        select("id=select_17", "index=2");
        assertText("id=cc_change", "change (select_17) 1 space");
        // spaces
        select("id=select_17", "index=3");
        assertText("id=cc_change", "change (select_17)  2 spaces");
        select("id=select_17", "index=4");
        assertText("id=cc_change", "change (select_17)  \\");
        select("id=select_17", "index=5");
        assertText("id=cc_change", "change (select_17)  ^");
        select("id=select_17", "index=6");
        assertText("id=cc_change", "glob:change (select_17)  regexp:[XYZ]{5}");
        select("id=select_17", "index=0");
        assertText("id=cc_change", "glob:change (select_17)  :");
        select("id=select_17", "index=7");
        assertText("id=cc_change", "glob:change (select_17)  select_17a");
        select("id=select_17", "index=8");
        assertText("id=cc_change", "glob:change (select_17)  select_17b");

    }
}