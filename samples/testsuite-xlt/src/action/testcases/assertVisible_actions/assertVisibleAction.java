package action.testcases.assertVisible_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertVisibleAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertVisibleAction(final AbstractHtmlPageAction prevAction)
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
        assertVisible("id=in_visible_anchor_vis");
        assertVisible("xpath=/html");
        assertVisible("xpath=id('select')/table[1]");
        assertVisible("name=anc_sel1");
        assertVisible("link=anc_sel1");
        assertVisible("id=in_txt_1");
        assertVisible("id=in_chk_1");
        assertVisible("id=fileInput");
        // assertVisible("id=invisible_empty_div");
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

        assertVisible("id=in_visible_anchor_vis");
        assertVisible("xpath=/html");
        assertVisible("xpath=id('select')/table[1]");
        assertVisible("name=anc_sel1");
        assertVisible("link=anc_sel1");
        assertVisible("id=in_txt_1");
        assertVisible("id=in_chk_1");
        assertVisible("id=fileInput");
        // assertVisible("id=invisible_empty_div");
    }
}