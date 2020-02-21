package action.modules.click_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class anchor extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public anchor(final AbstractHtmlPageAction prevAction)
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

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = click("id=anc_sel1");
        assertText("id=cc_click_head", "click (anc_sel1)");
        page = click("anc_sel2");
        assertText("id=cc_click_head", "click (anc_sel2)");
        page = click("name=anc_sel3");
        assertText("id=cc_click_head", "click (anc_sel3)");
        page = click("link=anc_sel4");
        assertText("id=cc_click_head", "click (anc_sel4)");
        page = click("css=#anchor_selector #anc_sel1");

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

        assertText("id=cc_click_head", "click (anc_sel1)");

    }
}