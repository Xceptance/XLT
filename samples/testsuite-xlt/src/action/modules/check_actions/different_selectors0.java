package action.modules.check_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class different_selectors0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public different_selectors0(final AbstractHtmlPageAction prevAction)
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
        page = uncheck("id=in_chk_7");
        assertText("id=cc_change", "change (in_chk_7) false");
        page = uncheck("name=in_chk_8");
        assertText("id=cc_change", "change (in_chk_8) false");
        page = uncheck("xpath=id('in_checkbox')/input[@value='in_chk_9' and @type='checkbox']");
        assertText("id=cc_change", "change (in_chk_9) false");
        page = uncheck("xpath=id('in_checkbox')/input[@type='checkbox'][10]");
        assertText("id=cc_change", "change (check plain) false");
        page = uncheck("dom=document.getElementById('in_chk_2')");

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

        assertText("id=cc_change", "change (in_chk_2) false");

    }
}