package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class MultiSelection_nonunique extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        addSelection("id=select_18", optionLocator + "=select_18");
        assertText("id=cc_change", "change (select_18) select_18a, select_18b");
        removeSelection("id=select_18", optionLocator + "=select_18");
        assertText("id=cc_change", "change (select_18)");

    }
}