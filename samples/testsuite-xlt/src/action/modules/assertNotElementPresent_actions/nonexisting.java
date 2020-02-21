package action.modules.assertNotElementPresent_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class nonexisting extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public nonexisting(final AbstractHtmlPageAction prevAction)
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
        assertNotElementPresent("id=anc");
        assertNotElementPresent("link=anc");
        assertNotElementPresent("name=anc");
        assertNotElementPresent("xpath=id('anc')");
        assertNotElementPresent("css=anc");
        assertNotElementPresent("anc");

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

        assertNotElementPresent("id=anc");
        assertNotElementPresent("link=anc");
        assertNotElementPresent("name=anc");
        assertNotElementPresent("xpath=id('anc')");
        assertNotElementPresent("css=anc");
        assertNotElementPresent("anc");

    }
}