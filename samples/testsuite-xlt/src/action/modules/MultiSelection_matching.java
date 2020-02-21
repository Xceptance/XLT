package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class MultiSelection_matching extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     * 
     */
    public MultiSelection_matching(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = addSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9) select_9_a");
        resultingPage = removeSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9)");

        return resultingPage;
    }
}