package action.testcases.assertAttribute_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class whitespaces extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public whitespaces(final AbstractHtmlPageAction prevAction)
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
        // contains whitespace
        assertAttribute("xpath=id('ws8_a')/input[2]@value", "foo bar");
        // start with whitespace
        assertAttribute("xpath=id('ws8_a')/input[3]@value", " foobar");
        // ends with whitespace
        assertAttribute("xpath=id('ws8_a')/input[4]@value", "foobar ");
        // whitespaces all around und in between
        assertAttribute("xpath=id('ws8_a')/input[5]@value", " foo bar ");
        // attribute consits of whitespaces only
        assertAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", "  ");

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

        // contains whitespace
        assertAttribute("xpath=id('ws8_a')/input[2]@value", "foo bar");
        // start with whitespace
        assertAttribute("xpath=id('ws8_a')/input[3]@value", " foobar");
        // ends with whitespace
        assertAttribute("xpath=id('ws8_a')/input[4]@value", "foobar ");
        // whitespaces all around und in between
        assertAttribute("xpath=id('ws8_a')/input[5]@value", " foo bar ");
        // attribute consits of whitespaces only
        assertAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", "  ");

    }
}