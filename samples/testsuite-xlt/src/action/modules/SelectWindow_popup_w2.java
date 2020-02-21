package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class SelectWindow_popup_w2 extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public SelectWindow_popup_w2()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = selectWindow("name=popup_w2");
        assertTitle("frame parent");

        return resultingPage;
    }
}