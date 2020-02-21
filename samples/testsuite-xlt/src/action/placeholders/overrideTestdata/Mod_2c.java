package action.placeholders.overrideTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * Use test data and define them.
 */
public class Mod_2c extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_2c()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertText("id=specialchar_1", resolve("${gtd1}"));
        resultingPage = type("id=in_txt_1", resolve("${t1} - 3"));

        return resultingPage;
    }
}