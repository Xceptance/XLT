package scripting.placeholders.overrideTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Mod_3 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        assertText("id=specialchar_3", resolve("${gtd2}"));

    }
}