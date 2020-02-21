package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class MultiSelection_specialChars extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];

        //
        // ~~~ add_special_chars_multiple ~~~
        //
        startAction("add_special_chars_multiple");
        addSelection("id=select_18", optionLocator + "=\\");
        assertText("id=cc_change", "change (select_18) \\");
        addSelection("id=select_18", optionLocator + "=^");
        assertText("id=cc_change", "change (select_18) \\, ^");
        addSelection("id=select_18", optionLocator + "=exact:regexp:[XYZ]{5}");
        assertText("id=cc_change", "glob:change (select_18) \\, ^, regexp:[XYZ]{5}");
        addSelection("id=select_18", optionLocator + "=:");
        assertText("id=cc_change", "glob:change (select_18) :, \\, ^, regexp:[XYZ]{5}");

        //
        // ~~~ remove_special_chars_multiple ~~~
        //
        startAction("remove_special_chars_multiple");
        removeSelection("id=select_18", optionLocator + "=\\");
        assertText("id=cc_change", "glob:change (select_18) :, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", optionLocator + "=^");
        assertText("id=cc_change", "glob:change (select_18) :, regexp:[XYZ]{5}");
        removeSelection("id=select_18", optionLocator + "=exact:regexp:[XYZ]{5}");
        assertText("id=cc_change", "glob:change (select_18) :");
        removeSelection("id=select_18", optionLocator + "=:");
        assertText("id=cc_change", "change (select_18)");

    }
}