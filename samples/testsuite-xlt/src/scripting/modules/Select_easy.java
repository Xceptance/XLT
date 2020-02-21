package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Select_easy extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        //
        // ~~~ letters_only ~~~
        //
        startAction("letters_only");
        select("id=select_1", optionLocator + "=select_1_b");
        assertText("id=cc_change", "change (select_1) select_1_b");
        //
        // ~~~ with_whitespace ~~~
        //
        startAction("with_whitespace");
        select("id=select_14", optionLocator + "=select_14 b");
        assertText("id=cc_change", "change (select_14) select_14 b");
        //
        // ~~~ double ~~~
        //
        startAction("double");
        select("id=select_1", optionLocator + "=select_1_c");
        select("id=select_1", optionLocator + "=select_1_c");
        assertText("id=cc_change", "change (select_1) select_1_c");
        select("id=select_14", optionLocator + "=select_14 b");
        assertText("id=cc_change", "change (select_1) select_1_c");

    }
}