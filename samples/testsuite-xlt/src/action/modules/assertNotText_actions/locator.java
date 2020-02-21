package action.modules.assertNotText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class locator extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public locator(final AbstractHtmlPageAction prevAction)
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
        assertNotText("id=anc_sel1", "anc");
        assertNotText("name=anc_sel1", "anc");
        assertNotText("link=anc_sel1", "anc");
        assertNotText("xpath=id('anc_sel1')", "anc");
        assertNotText("dom=document.getElementById('anc_sel1')", "anc");
        assertNotText("css=#anchor_selector #anc_sel1", "anc");

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

        assertNotText("id=anc_sel1", "anc");
        assertNotText("name=anc_sel1", "anc");
        assertNotText("link=anc_sel1", "anc");
        assertNotText("xpath=id('anc_sel1')", "anc");
        assertNotText("dom=document.getElementById('anc_sel1')", "anc");
        assertNotText("css=#anchor_selector #anc_sel1", "anc");

    }
}