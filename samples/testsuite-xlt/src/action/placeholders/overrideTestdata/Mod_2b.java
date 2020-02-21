package action.placeholders.overrideTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.placeholders.overrideTestdata.Mod_2c;

/**
 * Use test data and define them.
 */
public class Mod_2b extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_2b()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final Mod_2c mod_2c = new Mod_2c();
        resultingPage = mod_2c.run(resultingPage);

        assertText("id=specialchar_1", resolve("${gtd1}"));
        resultingPage = type("id=in_txt_1", resolve("${t1} - 2"));

        return resultingPage;
    }
}