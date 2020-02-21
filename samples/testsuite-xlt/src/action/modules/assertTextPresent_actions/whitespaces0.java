package action.modules.assertTextPresent_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class whitespaces0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public whitespaces0(final AbstractHtmlPageAction prevAction)
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
        assertTextPresent("This text contains just single spaces");
        assertTextPresent("This text contains multiple spaces");
        assertTextPresent("This text contains single tabulators");
        assertTextPresent("This text contains multiple tabulators");
        assertTextPresent("This text contains line breaks");
        assertTextPresent("This text contains single HTML encoded spaces");
        assertTextPresent("This text contains multiple HTML encoded spaces");
        assertTextPresent("This text contains alternating spaces");
        assertTextPresent("This text contains 274 spaces in row");
        assertTextPresent("This text contains mixed white spaces");
        assertTextPresent("This text contains paragraph tags.");
        assertTextPresent("This text contains HTML encoded line breaks.");
        assertTextPresent("This text contains many div tags.");
        assertTextPresent("Each word has its own div.");

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

        assertTextPresent("This text contains just single spaces");
        assertTextPresent("This text contains multiple spaces");
        assertTextPresent("This text contains single tabulators");
        assertTextPresent("This text contains multiple tabulators");
        assertTextPresent("This text contains line breaks");
        assertTextPresent("This text contains single HTML encoded spaces");
        assertTextPresent("This text contains multiple HTML encoded spaces");
        assertTextPresent("This text contains alternating spaces");
        assertTextPresent("This text contains 274 spaces in row");
        assertTextPresent("This text contains mixed white spaces");
        assertTextPresent("This text contains paragraph tags.");
        assertTextPresent("This text contains HTML encoded line breaks.");
        assertTextPresent("This text contains many div tags.");
        assertTextPresent("Each word has its own div.");

    }
}