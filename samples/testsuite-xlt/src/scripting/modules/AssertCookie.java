package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.ReadCookie;

/**
 * TODO: Add class description
 */
public class AssertCookie extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String name = parameters[0];
        final String value = parameters[1];
        final ReadCookie _readCookie = new ReadCookie();
        _readCookie.execute(name);

        assertText("id=cookieResult", value);

    }
}