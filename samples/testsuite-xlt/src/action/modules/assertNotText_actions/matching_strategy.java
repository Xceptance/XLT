package action.modules.assertNotText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class matching_strategy extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public matching_strategy(final AbstractHtmlPageAction prevAction)
    {
        super(prevAction);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);
        assertNotText("id=specialchar_1", "");
        assertNotText("id=specialchar_1", "glob:");
        assertNotText("id=specialchar_1", "exact:");
        assertNotText("id=specialchar_1", "glob:ipsum");
        assertNotText("id=specialchar_1", "ipsum");

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();

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

        assertNotText("id=specialchar_1", "");
        assertNotText("id=specialchar_1", "glob:");
        assertNotText("id=specialchar_1", "exact:");
        assertNotText("id=specialchar_1", "glob:ipsum");
        assertNotText("id=specialchar_1", "ipsum");

    }
}