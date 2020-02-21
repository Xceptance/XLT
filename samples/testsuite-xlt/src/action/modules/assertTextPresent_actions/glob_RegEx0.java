package action.modules.assertTextPresent_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class glob_RegEx0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public glob_RegEx0(final AbstractHtmlPageAction prevAction)
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
        assertTextPresent("Lorem ipsum * dolor sit amet");
        assertTextPresent("Lorem ipsum ??? dolor sit amet");
        assertTextPresent("regexp:Lorem ipsum [XYZ]{3} dolor sit amet");
        assertTextPresent("regexp:^.* [XYZ]{3} .*$");
        assertTextPresent("exact:Lorem ipsum XYZ dolor sit amet");
        assertTextPresent("glob:Lorem ipsum ??? dolor sit amet");

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

        assertTextPresent("Lorem ipsum * dolor sit amet");
        assertTextPresent("Lorem ipsum ??? dolor sit amet");
        assertTextPresent("regexp:Lorem ipsum [XYZ]{3} dolor sit amet");
        assertTextPresent("regexp:^.* [XYZ]{3} .*$");
        assertTextPresent("exact:Lorem ipsum XYZ dolor sit amet");
        assertTextPresent("glob:Lorem ipsum ??? dolor sit amet");

    }
}