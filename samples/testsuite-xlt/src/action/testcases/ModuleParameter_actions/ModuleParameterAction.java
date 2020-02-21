package action.testcases.ModuleParameter_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.SimpleMod_AssertText;

/**
 * TODO: Add class description
 */
public class ModuleParameterAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public ModuleParameterAction(final AbstractHtmlPageAction prevAction)
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
        final SimpleMod_AssertText simpleMod_AssertText = new SimpleMod_AssertText("specialchar_15", "Lorem ipsum\\");
        page = simpleMod_AssertText.run(page);

        final SimpleMod_AssertText simpleMod_AssertText0 = new SimpleMod_AssertText("specialchar_15", "regexp:Lorem\\sipsum.*");
        page = simpleMod_AssertText0.run(page);


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