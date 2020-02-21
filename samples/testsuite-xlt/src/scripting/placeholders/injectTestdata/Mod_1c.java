package scripting.placeholders.injectTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * Use test data but do not define them.
 */
public class Mod_1c extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        assertText("id=specialchar_1", resolve("${gtd2}"));
        type("id=in_txt_1", resolve("${t1}  - 3"));

    }
}