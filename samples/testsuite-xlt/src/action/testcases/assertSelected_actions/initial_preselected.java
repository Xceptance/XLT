package action.testcases.assertSelected_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.assertSelected;
import action.modules.assertNotSelected;

/**
 * TODO: Add class description
 */
public class initial_preselected extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public initial_preselected(final AbstractHtmlPageAction prevAction)
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
        final assertSelected assertSelected = new assertSelected("id=select_22", "select_22_c", "2");
        page = assertSelected.run(page);

        final assertNotSelected assertNotSelected = new assertNotSelected("id=select_22", "select_22_a", "0");
        page = assertNotSelected.run(page);

        final assertSelected assertSelected0 = new assertSelected("id=select_24", "select_24_c", "2");
        page = assertSelected0.run(page);

        final assertNotSelected assertNotSelected0 = new assertNotSelected("id=select_24", "select_24_a", "0");
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