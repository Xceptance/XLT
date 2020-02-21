package action.testcases.assertSelected_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.assertNotSelected;

/**
 * TODO: Add class description
 */
public class unselect_selected extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public unselect_selected(final AbstractHtmlPageAction prevAction)
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
        page = select("id=select_1", "id=select_1_d");
        page = removeSelection("id=select_9", "id=select_9_b");
        final assertNotSelected assertNotSelected = new assertNotSelected("id=select_1", "select_1_c", "2");
        page = assertNotSelected.run(page);

        final assertNotSelected assertNotSelected0 = new assertNotSelected("id=select_9", "select_9_b", "1");
        page = assertNotSelected0.run(page);


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