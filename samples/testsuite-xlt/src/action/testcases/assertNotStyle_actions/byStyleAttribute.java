package action.testcases.assertNotStyle_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class byStyleAttribute extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public byStyleAttribute(final AbstractHtmlPageAction prevAction)
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
        // inherited style only
        assertNotStyle("id=style_1_1", "fomt-size:11px");
        // own style, masked parent style
        assertNotStyle("id=style_1_2", "fomt-size:12px");
        // own style, no masked parent style
        assertNotStyle("id=style_1_3", "fomt-size:11px");

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

        // inherited style only
        assertNotStyle("id=style_1_1", "fomt-size:11px");
        // own style, masked parent style
        assertNotStyle("id=style_1_2", "fomt-size:12px");
        // own style, no masked parent style
        assertNotStyle("id=style_1_3", "fomt-size:11px");

    }
}