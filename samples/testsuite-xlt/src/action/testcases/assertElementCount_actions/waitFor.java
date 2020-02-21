package action.testcases.assertElementCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.StartAppear;

/**
 * TODO: Add class description
 */
public class waitFor extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitFor(final AbstractHtmlPageAction prevAction)
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
        final StartAppear startAppear = new StartAppear("1000");
        page = startAppear.run(page);

        page = waitForElementCount("id=appear_1", 1);
        page = waitForElementCount("name=appear_2", 1);
        page = waitForElementCount("link=appear_3 : anchor with link name", 1);
        page = waitForElementCount("xpath=id('appear_5')", 1);
        page = waitForElementCount("dom=document.getElementById('appear_6')", 1);
        page = waitForElementCount("css=.appear_7", 1);

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