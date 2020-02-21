package action.modules.deleteAllVisibleCookies_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class checkAbsence extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public checkAbsence(final AbstractHtmlPageAction prevAction)
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
        final AssertCookie assertCookie = new AssertCookie("testsuite-xlt_1", "");
        page = assertCookie.run(page);

        final AssertCookie assertCookie0 = new AssertCookie("testsuite-xlt_2", "");
        page = assertCookie0.run(page);

        final AssertCookie assertCookie1 = new AssertCookie("testsuite-xlt_3", "");
        page = assertCookie1.run(page);


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