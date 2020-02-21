package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class Mod_2 extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_2()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = type("id=in_txt_1", resolve("${td1} - 1"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl2 - 1");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        resultingPage = type("id=in_txt_1", resolve("${td2} - 1"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl1 - 1");
        assertText("id=specialchar_1", resolve("${gtd2}"));

        return resultingPage;
    }
}