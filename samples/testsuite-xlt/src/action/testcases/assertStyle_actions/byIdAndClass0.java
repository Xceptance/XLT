package action.testcases.assertStyle_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class byIdAndClass0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public byIdAndClass0(final AbstractHtmlPageAction prevAction)
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
        assertStyle("id=style_2_1", "font-size:12px");
        // own style, masked parent style
        assertStyle("id=style_2_2", "font-size:11px");
        // own style, no masked parent style
        assertStyle("id=style_2_3", "font-size:12px");

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
        assertStyle("id=style_2_1", "font-size:12px");
        // own style, masked parent style
        assertStyle("id=style_2_2", "font-size:11px");
        // own style, no masked parent style
        assertStyle("id=style_2_3", "font-size:12px");

    }
}