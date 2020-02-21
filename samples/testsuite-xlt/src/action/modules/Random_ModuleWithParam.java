package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class Random_ModuleWithParam extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'param' parameter.
     */
    private final String param;


    /**
     * Constructor.
     * @param param The 'param' parameter.
     * 
     */
    public Random_ModuleWithParam(final String param)
    {
        this.param = param;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = type("id=in_txt_1", param);
        assertText("id=in_txt_1", "exact:" + param);

        return resultingPage;
    }
}