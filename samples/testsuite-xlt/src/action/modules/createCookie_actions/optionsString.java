package action.modules.createCookie_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class optionsString extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public optionsString(final AbstractHtmlPageAction prevAction)
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
        createCookie("x_4=create_with_option_string", "path=/,max_age=10");
        final AssertCookie assertCookie = new AssertCookie("x_4", "create_with_option_string");
        page = assertCookie.run(page);

        // createCookie("x_5=^°!§$%&`´|üöäÜÖÄß+*~#'-_.\\");
        // final AssertCookie assertCookie0 = new AssertCookie("x_5","^°!§$%&`´|üöäÜÖÄß+*~#'-_.\\");
        // page = assertCookie0.run(page);

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