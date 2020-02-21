package action.placeholders.injectTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.placeholders.injectTestdata.Mod_1c;

/**
 * Use test data but do not define them.
 */
public class Mod_1b extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_1b()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final Mod_1c mod_1c = new Mod_1c();
        resultingPage = mod_1c.run(resultingPage);

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        resultingPage = type("id=in_txt_1", resolve("${t1}  - 2"));

        return resultingPage;
    }
}