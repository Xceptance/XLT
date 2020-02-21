package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class assertNotSelected extends AbstractWebDriverModule
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
        assertNotSelectedId(selectLocator, optionLocator);
        assertNotSelectedIndex(selectLocator, index);
        assertNotSelectedLabel(selectLocator, optionLocator);
        assertNotSelectedValue(selectLocator, optionLocator);

    }
}