package action.modules.assertText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class link extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public link(final AbstractHtmlPageAction prevAction)
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
        assertText("name=anc_sel1", "anc_sel1");
        assertText("link=anc_sel1", "anc_sel1");
        assertText("link=*_sel1", "anc_sel1");
        assertText("dom=document.getElementById('anc_sel1')", "anc_sel1");
        assertText("css=#anc_sel1", "anc_sel1");
        assertText("id=anc_sel7", "*sel7");

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

        assertText("name=anc_sel1", "anc_sel1");
        assertText("link=anc_sel1", "anc_sel1");
        assertText("link=*_sel1", "anc_sel1");
        assertText("dom=document.getElementById('anc_sel1')", "anc_sel1");
        assertText("css=#anc_sel1", "anc_sel1");
        assertText("id=anc_sel7", "*sel7");

    }
}