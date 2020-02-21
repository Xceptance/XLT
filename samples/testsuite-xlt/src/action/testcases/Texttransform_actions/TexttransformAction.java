package action.testcases.Texttransform_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class TexttransformAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public TexttransformAction(final AbstractHtmlPageAction prevAction)
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
        page = click("link=Text Transform");

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

        assertText("//*[@id='text-transform']/p[contains(@class,'upcase')][1]", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS.");
        assertText("//*[@id='text-transform']/p[contains(@class,'locase')][1]", "this text should be displayed in small letters.");
        assertText("//*[@id='text-transform']/p[contains(@class,'capital')][1]", "This Text Should Be Displayed In Capitalized Form.");
        assertText("id=text-transform", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS. this text should be displayed in small letters. This Text Should Be Displayed In Capitalized Form.");
        assertNotText("xpath=id('text-transform')/p[@class='upcase']", "This text should be displayed in capital letters.");
        assertNotText("xpath=id('text-transform')/p[@class='locase']", "THIS TEXT SHOULD BE DISPLAYED IN SMALL LETTERS.");
        assertNotText("xpath=id('text-transform')/p[@class='capital']", "this text should be displayed in capitalized form.");

    }
}