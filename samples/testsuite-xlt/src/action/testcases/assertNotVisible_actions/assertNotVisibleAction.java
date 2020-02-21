package action.testcases.assertNotVisible_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertNotVisibleAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertNotVisibleAction(final AbstractHtmlPageAction prevAction)
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
        assertNotVisible("id=in_visible_anchor_inv");
        assertNotVisible("id=invisible_visibility");
        assertNotVisible("id=invisible_visibility_style");
        assertNotVisible("id=invisible_display");
        assertNotVisible("id=invisible_hidden_input");

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

        assertNotVisible("id=in_visible_anchor_inv");
        assertNotVisible("id=invisible_visibility");
        assertNotVisible("id=invisible_visibility_style");
        assertNotVisible("id=invisible_display");
        assertNotVisible("id=invisible_hidden_input");

    }
}