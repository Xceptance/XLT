package scripting.placeholders.overrideTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.placeholders.overrideTestdata.Mod_2c;

/**
 * Use test data and define them.
 */
public class Mod_2b extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Mod_2c _mod_2c = new Mod_2c();
        _mod_2c.execute();

        assertText("id=specialchar_1", resolve("${gtd1}"));
        type("id=in_txt_1", resolve("${t1} - 2"));

    }
}