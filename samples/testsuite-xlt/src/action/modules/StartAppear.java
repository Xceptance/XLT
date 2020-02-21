package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.modules.SetGlobalTimeout;

/**
 * TODO: Add class description
 */
public class StartAppear extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'delay' parameter.
     */
    private final String delay;


    /**
     * Constructor.
     * @param delay The 'delay' parameter.
     * 
     */
    public StartAppear(final String delay)
    {
        this.delay = delay;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final SetGlobalTimeout setGlobalTimeout = new SetGlobalTimeout(delay);
        resultingPage = setGlobalTimeout.run(resultingPage);

        resultingPage = click("xpath=id('appear')/input[@value='appear' and @type='submit']");

        return resultingPage;
    }
}