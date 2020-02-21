package action.modules.select_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class locators0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public locators0(final AbstractHtmlPageAction prevAction)
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
        page = select("id=select_1", "label=select_1_b");
        assertText("id=cc_change", "change (select_1) select_1_b");
        page = select("name=select_2", "label=select_2_b");
        assertText("id=cc_change", "change (select_2) select_2_b");
        page = select("xpath=id('select_1')", "label=select_1_c");
        assertText("id=cc_change", "change (select_1) select_1_c");
        page = select("css=select#select_2", "label=select_2_a");
        assertText("id=cc_change", "change (select_2) select_2_a");
        page = select("dom=document.getElementById('select_1')", "label=select_1_a");

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

        assertText("id=cc_change", "change (select_1) select_1_a");

    }
}