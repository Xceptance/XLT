package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Select_specialChar extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        //
        // ~~~ special_chars ~~~
        //
        startAction("special_chars");
        select("id=select_17", optionLocator + "=\\");
        assertText("id=cc_change", "change (select_17) \\");
        select("id=select_17", optionLocator + "=^");
        assertText("id=cc_change", "change (select_17) ^");
        select("id=select_17", optionLocator + "=exact:regexp:[XYZ]{5}");
        assertText("id=cc_change", "exact:change (select_17) regexp:[XYZ]{5}");
        select("id=select_17", optionLocator + "=:");
        assertText("id=cc_change", "glob:change (select_17) :");

    }
}