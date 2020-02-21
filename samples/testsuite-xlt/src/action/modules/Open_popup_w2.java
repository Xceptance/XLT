package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class Open_popup_w2 extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Open_popup_w2()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = click("id=popup_w2");
        waitForPopUp("popup_w2");

        return resultingPage;
    }
}