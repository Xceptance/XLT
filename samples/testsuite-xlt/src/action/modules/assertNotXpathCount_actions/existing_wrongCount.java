package action.modules.assertNotXpathCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class existing_wrongCount extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public existing_wrongCount(final AbstractHtmlPageAction prevAction)
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
        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", 0);
        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", 4);
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", 6);
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", 2147483647);

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

        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", 0);
        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", 4);
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", 6);
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", 2147483647);

    }
}