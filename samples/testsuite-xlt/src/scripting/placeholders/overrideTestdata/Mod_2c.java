package scripting.placeholders.overrideTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * Use test data and define them.
 */
public class Mod_2c extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        assertText("id=specialchar_1", resolve("${gtd1}"));
        type("id=in_txt_1", resolve("${t1} - 3"));

    }
}