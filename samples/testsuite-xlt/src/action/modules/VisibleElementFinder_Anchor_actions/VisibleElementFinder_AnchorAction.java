package action.modules.VisibleElementFinder_Anchor_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class VisibleElementFinder_AnchorAction extends AbstractHtmlUnitScriptAction
{

    /**
     * The 'locator' parameter.
     */
    private final String locator;

    /**
     * The 'title' parameter.
     */
    private final String title;

    /**
     * Constructor.
     * @param prevAction The previous action.
     * @param locator The 'locator' parameter.
     * @param title The 'title' parameter.
     */
    public VisibleElementFinder_AnchorAction(final AbstractHtmlPageAction prevAction, final String locator, final String title)
    {
        super(prevAction);
        this.locator = locator;
        this.title = title;
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
        page = clickAndWait(locator);

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

        assertTitle(title);

    }
}