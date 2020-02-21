package action.modules.waitFor_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.StartAppear;

/**
 * TODO: Add class description
 */
public class waitForAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForAction(final AbstractHtmlPageAction prevAction)
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

        page = waitForElementPresent("id=appear_1");
        page = waitForElementPresent("name=appear_2");
        page = waitForElementPresent("link=appear_3*");
        page = waitForElementPresent("xpath=//div[@id='appear']//div[2]");
        page = waitForElementPresent("dom=document.getElementById('appear_5')");
        page = waitForText("id=appear_7", "glob:appear_7 : paragraph");
        page = waitForTitle("appear_8");
        page = waitForAttribute("xpath=id('appear_9')@name", "text");
        page = waitForTextPresent("appear_10 link 3");
        page = waitForXpathCount("//div[@id='appear']/a[@name='appear_10']", 4);
        page = waitForClass("xpath=//input[@id='appear_9']", "appear_11");
        page = waitForStyle("css=#appear_9", "color: rgb(0, 191, 255)");

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