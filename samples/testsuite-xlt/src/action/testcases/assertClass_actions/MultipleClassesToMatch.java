package action.testcases.assertClass_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class MultipleClassesToMatch extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public MultipleClassesToMatch(final AbstractHtmlPageAction prevAction)
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
        assertClass("id=common_confirmation_area", "confirmation_area common_confirmation_area");
        assertClass("dom=document.getElementById('common_confirmation_area')", "confirmation_area");
        assertClass("css=#common_confirmation_area", "common_confirmation_area");

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

        assertClass("id=common_confirmation_area", "confirmation_area common_confirmation_area");
        assertClass("dom=document.getElementById('common_confirmation_area')", "confirmation_area");
        assertClass("css=#common_confirmation_area", "common_confirmation_area");

    }
}