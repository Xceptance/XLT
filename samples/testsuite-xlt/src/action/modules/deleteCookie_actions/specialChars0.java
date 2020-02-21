package action.modules.deleteCookie_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class specialChars0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public specialChars0(final AbstractHtmlPageAction prevAction)
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
        createCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.=^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        final AssertCookie assertCookie = new AssertCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.", "^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        page = assertCookie.run(page);

        deleteCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        final AssertCookie assertCookie0 = new AssertCookie("testsuite-xlt", "");
        page = assertCookie0.run(page);


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