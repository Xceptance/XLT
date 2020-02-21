package scripting.placeholders.injectTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.placeholders.injectTestdata.Mod_1b;

/**
 * Use test data but do not define them.
 */
public class Mod_1a extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Mod_1b _mod_1b = new Mod_1b();
        _mod_1b.execute();

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        type("id=in_txt_1", resolve("${t1} - 1"));

    }
}