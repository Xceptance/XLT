package action.testcases.assertNotAttribute_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertNotAttributeAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertNotAttributeAction(final AbstractHtmlPageAction prevAction)
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
        // 1 vs 2 whitespaces
        assertNotAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", " ");
        // 2 vs 1 whitespace
        assertNotAttribute("xpath=id('select_17')/option[@title='1 space']@value", "  ");
        // empty attribute value must not match any sign
        assertNotAttribute("xpath=id('in_txt_13')@value", "regexp:.+");
        // substring (max length n-1) must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "foo");
        // any single character must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "?");

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

        // 1 vs 2 whitespaces
        assertNotAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", " ");
        // 2 vs 1 whitespace
        assertNotAttribute("xpath=id('select_17')/option[@title='1 space']@value", "  ");
        // empty attribute value must not match any sign
        assertNotAttribute("xpath=id('in_txt_13')@value", "regexp:.+");
        // substring (max length n-1) must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "foo");
        // any single character must not match
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "?");

    }
}