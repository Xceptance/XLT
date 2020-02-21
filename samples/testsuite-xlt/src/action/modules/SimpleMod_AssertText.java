package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class SimpleMod_AssertText extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'id' parameter.
     */
    private final String id;

    /**
     * The 'value' parameter.
     */
    private final String value;


    /**
     * Constructor.
     * @param id The 'id' parameter.
     * @param value The 'value' parameter.
     * 
     */
    public SimpleMod_AssertText(final String id, final String value)
    {
        this.id = id;
        this.value = value;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertText("id=" + id, value);

        return resultingPage;
    }
}