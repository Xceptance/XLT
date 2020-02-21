package action.modules.waitForNot_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.StartDisappear;

/**
 * TODO: Add class description
 */
public class waitForNotAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForNotAction(final AbstractHtmlPageAction prevAction)
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

        page = waitForNotTitle("example page");
        page = waitForNotElementPresent("id=disapp_1");
        page = waitForNotElementPresent("name=disapp_2");
        page = waitForNotElementPresent("link=disapp_3");
        page = waitForNotElementPresent("xpath=id('disapp_4')");
        page = waitForNotElementPresent("dom=document.getElementById('disapp_5')");
        page = waitForNotText("id=disapp_6", "glob:disapp_6 : paragraph*");
        page = waitForNotXpathCount("//div[@id='disappear']/a[@name='disapp_9']", 3);
        page = waitForNotXpathCount("//div[@id='disappear']/a[@name='disapp_9']", 2147483647);
        page = waitForNotTextPresent("disapp_8 xcount");
        page = waitForNotAttribute("xpath=id('disapp_10')@name", "disapp_10");
        page = waitForNotClass("id=disapp_11", "disapp_11");
        page = waitForNotStyle("css=#disapp_11", "color: rgb(0, 191, 255)");

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