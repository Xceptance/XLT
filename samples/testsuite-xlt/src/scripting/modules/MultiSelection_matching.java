package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class MultiSelection_matching extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        addSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9) select_9_a");
        removeSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9)");

    }
}