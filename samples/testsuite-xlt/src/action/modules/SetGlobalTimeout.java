package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class SetGlobalTimeout extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'timeout' parameter.
     */
    private final String timeout;


    /**
     * Constructor.
     * @param timeout The 'timeout' parameter.
     * 
     */
    public SetGlobalTimeout(final String timeout)
    {
        this.timeout = timeout;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = type("id=timeout_field", timeout);
        resultingPage = click("xpath=/html/body/ul/li[3]/a");

        return resultingPage;
    }
}