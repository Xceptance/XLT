package action.placeholders.injectTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.placeholders.injectTestdata.Mod_1b;

/**
 * Use test data but do not define them.
 */
public class Mod_1a extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_1a()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final Mod_1b mod_1b = new Mod_1b();
        resultingPage = mod_1b.run(resultingPage);

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        resultingPage = type("id=in_txt_1", resolve("${t1} - 1"));

        return resultingPage;
    }
}