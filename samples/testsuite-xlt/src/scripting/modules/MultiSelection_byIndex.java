package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class MultiSelection_byIndex extends AbstractWebDriverModule
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
        // ~~~ add_specialChars_byIndex ~~~
        //
        startAction("add_specialChars_byIndex");
        addSelection("id=select_18", "index=1");
        assertText("id=cc_change", "change (select_18) empty");
        addSelection("id=select_18", "index=2");
        assertText("id=cc_change", "change (select_18) empty, 1 space");
        addSelection("id=select_18", "index=3");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces");
        addSelection("id=select_18", "index=4");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces, \\");
        addSelection("id=select_18", "index=5");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces, \\, ^");
        addSelection("id=select_18", "index=6");
        assertText("id=cc_change", "glob:change (select_18) empty, 1 space, 2 spaces, \\, ^, regexp:[XYZ]{5}");
        addSelection("id=select_18", "index=0");
        assertText("id=cc_change", "glob:change (select_18) :, empty, 1 space, 2 spaces, \\, ^, regexp:[XYZ]{5}");

        //
        // ~~~ remove_specialChars_byIndex ~~~
        //
        startAction("remove_specialChars_byIndex");
        // space
        removeSelection("id=select_18", "index=1");
        assertText("id=cc_change", "glob:change (select_18) :, 1 space, 2 spaces, \\, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", "index=2");
        assertText("id=cc_change", "glob:change (select_18) :, 2 spaces, \\, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", "index=3");
        assertText("id=cc_change", "glob:change (select_18) :, \\, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", "index=4");
        assertText("id=cc_change", "glob:change (select_18) :, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", "index=5");
        assertText("id=cc_change", "glob:change (select_18) :, regexp:[XYZ]{5}");
        removeSelection("id=select_18", "index=6");
        assertText("id=cc_change", "glob:change (select_18) :");
        removeSelection("id=select_18", "index=0");
        assertText("id=cc_change", "change (select_18)");

        //
        // ~~~ double_select_byIndex ~~~
        //
        startAction("double_select_byIndex");
        addSelection("id=select_18", "index=7");
        assertText("id=cc_change", "change (select_18) select_18a");
        addSelection("id=select_18", "index=7");
        assertText("id=cc_change", "change (select_18) select_18a");
        removeSelection("id=select_18", "index=7");
        assertText("id=cc_change", "change (select_18)");

        //
        // ~~~ remove_unselected_byIndex ~~~
        //
        startAction("remove_unselected_byIndex");
        removeSelection("id=select_18", "index=7");
        assertText("id=cc_change", "change (select_18)");

    }
}