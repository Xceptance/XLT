package action.testcases.assertNotClass_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class SingleClassToMatch0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public SingleClassToMatch0(final AbstractHtmlPageAction prevAction)
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
        assertNotClass("xpath=//div[@id='appear' and contains(@class,'cat')]/..", "cat");
        assertNotClass("css= input[value='appear']", "cat");
        assertNotClass("xpath=id('anchor_list')", "anchor_list");
        assertNotClass("//div[@id='anchor_list']/ol[1]/li[2]", "a");

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

        assertNotClass("xpath=//div[@id='appear' and contains(@class,'cat')]/..", "cat");
        assertNotClass("css= input[value='appear']", "cat");
        assertNotClass("xpath=id('anchor_list')", "anchor_list");
        assertNotClass("//div[@id='anchor_list']/ol[1]/li[2]", "a");

    }
}