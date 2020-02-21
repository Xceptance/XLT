package action.modules.waitForNot_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class waitForNotSelected extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForNotSelected(final AbstractHtmlPageAction prevAction)
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
        page = waitForNotSelectedId("id=select_22", "select_22_c");
        page = click("id=select_22_d_delayedSelect");
        page = waitForNotSelectedIndex("id=select_22", "0");
        page = click("id=select_22_a_delayedSelect");
        page = waitForNotSelectedLabel("id=select_22", "select_22_d");
        page = click("id=select_22_d_delayedSelect");
        page = waitForNotSelectedValue("id=select_22", "select_22_a");
        page = click("id=select_24_a_delayedSelect");
        page = waitForNotSelectedId("id=select_24", "select_24_c");
        page = click("id=select_24_d_delayedSelect");
        page = waitForNotSelectedIndex("id=select_24", "0");
        page = click("id=select_24_a_delayedSelect");
        page = waitForNotSelectedLabel("id=select_24", "select_24_d");
        page = click("id=select_24_d_delayedSelect");
        page = waitForNotSelectedValue("id=select_24", "select_24_a");

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