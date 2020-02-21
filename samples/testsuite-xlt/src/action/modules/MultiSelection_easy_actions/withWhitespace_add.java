package action.modules.MultiSelection_easy_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class withWhitespace_add extends AbstractHtmlUnitScriptAction
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
    public withWhitespace_add(final AbstractHtmlPageAction prevAction, final String optionLocator)
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
        page = addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 a");
        assertText("id=cc_change", "change (select_16) select_16 a");
        page = addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 b");
        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b");
        page = addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 c");

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

        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b, select_16 c");

    }
}