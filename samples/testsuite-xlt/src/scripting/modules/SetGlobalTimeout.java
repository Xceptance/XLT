package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class SetGlobalTimeout extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String timeout = parameters[0];
        type("id=timeout_field", timeout);
        click("xpath=/html/body/ul/li[3]/a");

    }
}