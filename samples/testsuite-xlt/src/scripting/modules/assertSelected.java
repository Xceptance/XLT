package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class assertSelected extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String selectLocator = parameters[0];
        final String optionLocator = parameters[1];
        final String index = parameters[2];
        assertSelectedId(selectLocator, optionLocator);
        assertSelectedIndex(selectLocator, index);
        assertSelectedLabel(selectLocator, optionLocator);
        assertSelectedValue(selectLocator, optionLocator);

    }
}