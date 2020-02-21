package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class Mod_2 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        type("id=in_txt_1", resolve("${td1} - 1"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl2 - 1");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        type("id=in_txt_1", resolve("${td2} - 1"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl1 - 1");
        assertText("id=specialchar_1", resolve("${gtd2}"));

    }
}