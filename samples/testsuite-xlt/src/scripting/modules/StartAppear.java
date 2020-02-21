package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.SetGlobalTimeout;

/**
 * TODO: Add class description
 */
public class StartAppear extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String delay = parameters[0];
        final SetGlobalTimeout _setGlobalTimeout = new SetGlobalTimeout();
        _setGlobalTimeout.execute(delay);

        click("xpath=id('appear')/input[@value='appear' and @type='submit']");

    }
}