package action.placeholders.overrideTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.placeholders.overrideTestdata.Mod_2b;

/**
 * Use test data and define them.
 */
public class Mod_2a extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_2a()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final Mod_2b mod_2b = new Mod_2b();
        resultingPage = mod_2b.run(resultingPage);

        assertText("id=specialchar_1", resolve("${gtd1}"));
        resultingPage = type("id=in_txt_1", resolve("${t1} - 1"));

        return resultingPage;
    }
}