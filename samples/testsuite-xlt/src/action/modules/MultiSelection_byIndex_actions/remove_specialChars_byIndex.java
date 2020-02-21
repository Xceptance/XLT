package action.modules.MultiSelection_byIndex_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class remove_specialChars_byIndex extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public remove_specialChars_byIndex(final AbstractHtmlPageAction prevAction)
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
        // space
        page = removeSelection("id=select_18", "index=1");
        assertText("id=cc_change", "glob:change (select_18) :, 1 space, 2 spaces, \\, ^, regexp:[XYZ]{5}");
        page = removeSelection("id=select_18", "index=2");
        assertText("id=cc_change", "glob:change (select_18) :, 2 spaces, \\, ^, regexp:[XYZ]{5}");
        page = removeSelection("id=select_18", "index=3");
        assertText("id=cc_change", "glob:change (select_18) :, \\, ^, regexp:[XYZ]{5}");
        page = removeSelection("id=select_18", "index=4");
        assertText("id=cc_change", "glob:change (select_18) :, ^, regexp:[XYZ]{5}");
        page = removeSelection("id=select_18", "index=5");
        assertText("id=cc_change", "glob:change (select_18) :, regexp:[XYZ]{5}");
        page = removeSelection("id=select_18", "index=6");
        assertText("id=cc_change", "glob:change (select_18) :");
        page = removeSelection("id=select_18", "index=0");

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

        assertText("id=cc_change", "change (select_18)");

    }
}