package action.testcases.assertAttribute_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class implicit_glob extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public implicit_glob(final AbstractHtmlPageAction prevAction)
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
        // substring (starting with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foo*");
        // substring (ending with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*bar");
        // substring (contains)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*oo*");
        // single char wildcard
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "?oo?ar");

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

        // substring (starting with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foo*");
        // substring (ending with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*bar");
        // substring (contains)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*oo*");
        // single char wildcard
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "?oo?ar");

    }
}