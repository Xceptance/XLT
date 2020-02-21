package action.modules.waitFor_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class waitForSelected extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForSelected(final AbstractHtmlPageAction prevAction)
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
        page = click("id=select_22_a_delayedSelect");
        page = waitForSelectedId("id=select_22", "select_22_a");
        page = click("id=select_22_d_delayedSelect");
        page = waitForSelectedIndex("id=select_22", "3");
        page = click("id=select_22_a_delayedSelect");
        page = waitForSelectedLabel("id=select_22", "select_22_a");
        page = click("id=select_22_d_delayedSelect");
        page = waitForSelectedValue("id=select_22", "select_22_d");
        page = click("id=select_24_a_delayedSelect");
        page = waitForSelectedId("id=select_24", "select_24_a");
        page = click("id=select_24_d_delayedSelect");
        page = waitForSelectedIndex("id=select_24", "3");
        page = click("id=select_24_a_delayedSelect");
        page = waitForSelectedLabel("id=select_24", "select_24_a");
        page = click("id=select_24_d_delayedSelect");
        page = waitForSelectedValue("id=select_24", "select_24_d");

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