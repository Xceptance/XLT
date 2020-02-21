package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Select_matching extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        //
        // ~~~ matching ~~~
        //
        startAction("matching");
        select("id=select_1", optionLocator + "=select_1_?");
        assertText("id=cc_change", "change (select_1) select_1_a");
        select("id=select_1", optionLocator + "=regexp:select_1_[cx]");
        assertText("id=cc_change", "change (select_1) select_1_c");

    }
}