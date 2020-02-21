package action.modules.assertElementPresent_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class anchor_link extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public anchor_link(final AbstractHtmlPageAction prevAction)
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
        assertElementPresent("id=anc_sel1");
        assertElementPresent("name=anc_sel1");
        assertElementPresent("link=anc_sel1");
        assertElementPresent("xpath=id('anc_sel1')");
        assertElementPresent("dom=document.getElementById('anc_sel1')");
        assertElementPresent("css=#anchor_selector #anc_sel1");
        assertElementPresent("anc_sel1");

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

        assertElementPresent("id=anc_sel1");
        assertElementPresent("name=anc_sel1");
        assertElementPresent("link=anc_sel1");
        assertElementPresent("xpath=id('anc_sel1')");
        assertElementPresent("dom=document.getElementById('anc_sel1')");
        assertElementPresent("css=#anchor_selector #anc_sel1");
        assertElementPresent("anc_sel1");

    }
}