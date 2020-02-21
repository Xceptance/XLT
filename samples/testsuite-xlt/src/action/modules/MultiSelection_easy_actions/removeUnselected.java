package action.modules.MultiSelection_easy_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class removeUnselected extends AbstractHtmlUnitScriptAction
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;

    /**
     * Constructor.
     * @param prevAction The previous action.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public removeUnselected(final AbstractHtmlPageAction prevAction, final String optionLocator)
    {
        super(prevAction);
        this.optionLocator = optionLocator;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = removeSelection("id=select_9", optionLocator + "=select_9_b");

        setHtmlPage(page);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        Assert.assertNotNull("Failed to load page", page);

        assertText("id=cc_change", "change (select_9)");

    }
}