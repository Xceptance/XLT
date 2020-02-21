package action.testcases.assertClass_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class SingleClassToMatch extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public SingleClassToMatch(final AbstractHtmlPageAction prevAction)
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
        assertClass("xpath=//div[@id='appear' and contains(@class,'cat')][1]", "cat");
        assertClass("//ol[@id='mainList']/li[4]/div", "cat");
        assertClass("xpath=id('appear')", "cat");
        assertClass("//div[@id='anchor_list']/ol[1]/li[1]", "a");

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

        assertClass("xpath=//div[@id='appear' and contains(@class,'cat')][1]", "cat");
        assertClass("//ol[@id='mainList']/li[4]/div", "cat");
        assertClass("xpath=id('appear')", "cat");
        assertClass("//div[@id='anchor_list']/ol[1]/li[1]", "a");

    }
}