package action.modules.assertNotText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class case_insensitive extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public case_insensitive(final AbstractHtmlPageAction prevAction)
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
        assertNotText("id=specialchar_1", "*LOREM IPSUM*");
        assertNotText("id=specialchar_1", "regexp:.*LOREM IPSUM.*");
        assertNotText("id=specialchar_1", "regexpi:.*LOREM PSUM.*");

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

        assertNotText("id=specialchar_1", "*LOREM IPSUM*");
        assertNotText("id=specialchar_1", "regexp:.*LOREM IPSUM.*");
        assertNotText("id=specialchar_1", "regexpi:.*LOREM PSUM.*");

    }
}