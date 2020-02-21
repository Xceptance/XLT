package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Select_nonunique extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        //
        // ~~~ nonunique ~~~
        //
        startAction("nonunique");
        select("id=select_17", optionLocator + "=select_17");
        assertText("id=cc_change", "change (select_17) select_17a");
        //
        // ~~~ multi_select ~~~
        //
        startAction("multi_select");
        select("id=select_18", optionLocator + "=select_18");
        assertText("id=cc_change", "change (select_18) select_18a, select_18b");

    }
}