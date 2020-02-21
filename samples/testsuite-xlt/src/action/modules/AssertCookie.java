package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.modules.ReadCookie;

/**
 * TODO: Add class description
 */
public class AssertCookie extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'name' parameter.
     */
    private final String name;

    /**
     * The 'value' parameter.
     */
    private final String value;


    /**
     * Constructor.
     * @param name The 'name' parameter.
     * @param value The 'value' parameter.
     * 
     */
    public AssertCookie(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final ReadCookie readCookie = new ReadCookie(name);
        resultingPage = readCookie.run(resultingPage);

        assertText("id=cookieResult", value);

        return resultingPage;
    }
}