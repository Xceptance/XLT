package scripting.placeholders.overrideTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.placeholders.overrideTestdata.Mod_2b;

/**
 * Use test data and define them.
 */
public class Mod_2a extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Mod_2b _mod_2b = new Mod_2b();
        _mod_2b.execute();

        assertText("id=specialchar_1", resolve("${gtd1}"));
        type("id=in_txt_1", resolve("${t1} - 1"));

    }
}