package scripting.placeholders.injectTestdata;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.placeholders.injectTestdata.Mod_1c;

/**
 * Use test data but do not define them.
 */
public class Mod_1b extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Mod_1c _mod_1c = new Mod_1c();
        _mod_1c.execute();

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        type("id=in_txt_1", resolve("${t1}  - 2"));

    }
}