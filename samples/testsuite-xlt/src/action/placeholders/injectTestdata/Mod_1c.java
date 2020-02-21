package action.placeholders.injectTestdata;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * Use test data but do not define them.
 */
public class Mod_1c extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_1c()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertText("id=specialchar_1", resolve("${gtd2}"));
        resultingPage = type("id=in_txt_1", resolve("${t1}  - 3"));

        return resultingPage;
    }
}