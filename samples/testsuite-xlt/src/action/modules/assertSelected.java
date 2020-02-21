package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class assertSelected extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'selectLocator' parameter.
     */
    private final String selectLocator;

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;

    /**
     * The 'index' parameter.
     */
    private final String index;


    /**
     * Constructor.
     * @param selectLocator The 'selectLocator' parameter.
     * @param optionLocator The 'optionLocator' parameter.
     * @param index The 'index' parameter.
     * 
     */
    public assertSelected(final String selectLocator, final String optionLocator, final String index)
    {
        this.selectLocator = selectLocator;
        this.optionLocator = optionLocator;
        this.index = index;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertSelectedId(selectLocator, optionLocator);
        assertSelectedIndex(selectLocator, index);
        assertSelectedLabel(selectLocator, optionLocator);
        assertSelectedValue(selectLocator, optionLocator);

        return resultingPage;
    }
}