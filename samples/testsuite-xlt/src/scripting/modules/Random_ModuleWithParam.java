package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Random_ModuleWithParam extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String param = parameters[0];
        type("id=in_txt_1", param);
        assertText("id=in_txt_1", "exact:" + param);

    }
}