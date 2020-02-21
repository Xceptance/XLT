package action.testcases.assertElementCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.StartDisappear;

/**
 * TODO: Add class description
 */
public class waitForNot extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForNot(final AbstractHtmlPageAction prevAction)
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
        final StartDisappear startDisappear = new StartDisappear("1000");
        page = startDisappear.run(page);

        page = waitForNotElementCount("id=disapp_1", 1);
        page = waitForNotElementCount("name=disapp_2", 1);
        page = waitForNotElementCount("link=disapp_3", 1);
        page = waitForNotElementCount("xpath=id('disapp_4')", 1);
        page = waitForNotElementCount("dom=document.getElementById('disapp_5')", 1);
        page = waitForNotElementCount("css=.disapp_7", 1);

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


    }
}