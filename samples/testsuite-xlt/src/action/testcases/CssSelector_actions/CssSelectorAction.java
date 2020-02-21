package action.testcases.CssSelector_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class CssSelectorAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public CssSelectorAction(final AbstractHtmlPageAction prevAction)
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
        // wildcard
        assertElementPresent("css=*");
        // select by tag name
        assertElementPresent("css=div");
        // select by ID
        assertElementPresent("css=#mainList");
        // select by class
        assertElementPresent("css=.common_confirmation_area");
        // child
        assertElementPresent("css=div li");
        // direct child
        assertElementPresent("css=div > ul");
        // direct child (multiple)
        assertElementPresent("css=#mainList>li>h1");
        // first-child
        assertText("css=#mainList>li:first-child>h1", "configuration");
        // last-child
        assertText("css=#mainList>li:last-child>h1", "Stale");
        // nth-child
        assertText("css=#mainList>li:nth-child(3)>h1", "disappear");
        // select by attribute name
        assertElementPresent("css=select[id]");
        // select by attribute value
        assertElementPresent("css=select[id=select_1]");
        // select element by attribute list value
        assertElementPresent("css=div[class~=common_confirmation_area]");
        // Element with [class~=value]
        assertElementPresent("css=DIV.common_confirmation_area");
        // certain element with certain ID
        assertElementPresent("css=select#select_1");

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

        // wildcard
        assertElementPresent("css=*");
        // select by tag name
        assertElementPresent("css=div");
        // select by ID
        assertElementPresent("css=#mainList");
        // select by class
        assertElementPresent("css=.common_confirmation_area");
        // child
        assertElementPresent("css=div li");
        // direct child
        assertElementPresent("css=div > ul");
        // direct child (multiple)
        assertElementPresent("css=#mainList>li>h1");
        // first-child
        assertText("css=#mainList>li:first-child>h1", "configuration");
        // last-child
        assertText("css=#mainList>li:last-child>h1", "Stale");
        // nth-child
        assertText("css=#mainList>li:nth-child(3)>h1", "disappear");
        // select by attribute name
        assertElementPresent("css=select[id]");
        // select by attribute value
        assertElementPresent("css=select[id=select_1]");
        // select element by attribute list value
        assertElementPresent("css=div[class~=common_confirmation_area]");
        // Element with [class~=value]
        assertElementPresent("css=DIV.common_confirmation_area");
        // certain element with certain ID
        assertElementPresent("css=select#select_1");

    }
}