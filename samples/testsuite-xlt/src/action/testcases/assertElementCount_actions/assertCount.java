package action.testcases.assertElementCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertCount extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertCount(final AbstractHtmlPageAction prevAction)
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
        assertElementCount("id=in_txt_1", 1);
        assertElementCount("xpath=id('in_txt_1')", 1);
        assertElementCount("css=.disapp_11", 1);
        assertElementCount("name=in_txt_1", 1);
        assertElementCount("link=anc_sel1", 1);
        assertElementCount("dom=document.getElementById('in_txt_1')", 1);

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

        assertElementCount("id=in_txt_1", 1);
        assertElementCount("xpath=id('in_txt_1')", 1);
        assertElementCount("css=.disapp_11", 1);
        assertElementCount("name=in_txt_1", 1);
        assertElementCount("link=anc_sel1", 1);
        assertElementCount("dom=document.getElementById('in_txt_1')", 1);

    }
}