package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class ReadCookie extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'name' parameter.
     */
    private final String name;


    /**
     * Constructor.
     * @param name The 'name' parameter.
     * 
     */
    public ReadCookie(final String name)
    {
        this.name = name;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = type("id=cookieName", name);
        resultingPage = click("id=cookieRead");

        return resultingPage;
    }
}