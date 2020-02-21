package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class MultiSelection_nonunique extends AbstractHtmlUnitCommandsModule
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
    public MultiSelection_nonunique(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = addSelection("id=select_18", optionLocator + "=select_18");
        assertText("id=cc_change", "change (select_18) select_18a, select_18b");
        resultingPage = removeSelection("id=select_18", optionLocator + "=select_18");
        assertText("id=cc_change", "change (select_18)");

        return resultingPage;
    }
}