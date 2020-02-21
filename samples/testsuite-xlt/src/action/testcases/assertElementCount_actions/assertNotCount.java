package action.testcases.assertElementCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertNotCount extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertNotCount(final AbstractHtmlPageAction prevAction)
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
        assertNotElementCount("id=in_txt_1", 0);
        assertNotElementCount("xpath=id('in_txt_1')", 2);
        assertNotElementCount("css=.appear_11", 2);
        assertNotElementCount("name=in_txt_1", 2);
        assertNotElementCount("link=anc_sel1", 2);
        assertNotElementCount("dom=document.getElementById('in_txt_1')", 2);

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

        assertNotElementCount("id=in_txt_1", 0);
        assertNotElementCount("xpath=id('in_txt_1')", 2);
        assertNotElementCount("css=.appear_11", 2);
        assertNotElementCount("name=in_txt_1", 2);
        assertNotElementCount("link=anc_sel1", 2);
        assertNotElementCount("dom=document.getElementById('in_txt_1')", 2);

    }
}