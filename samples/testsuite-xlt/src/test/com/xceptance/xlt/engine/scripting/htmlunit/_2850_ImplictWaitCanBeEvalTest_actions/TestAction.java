package test.com.xceptance.xlt.engine.scripting.htmlunit._2850_ImplictWaitCanBeEvalTest_actions;

import java.net.URL;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestAction extends AbstractHtmlUnitScriptAction
{
    /**
     * Start URL as string.
     */
    private final String urlString;

    /**
     * Start URL as URL object.
     */
    private URL url;

    /**
     * Constructor.
     * 
     * @param prevAction
     *            The previous action.
     * @param urlString
     *            The start URL as string.
     */
    public TestAction(final AbstractHtmlPageAction prevAction, final String urlString)
    {
        super(prevAction);
        this.urlString = urlString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final String baseURL = getBaseUrl();
        if (baseURL != null && baseURL.trim().length() > 0)
        {
            url = new URL(new URL(baseURL), urlString);
        }
        else
        {
            url = new URL(urlString);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page;
        page = open(url);

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

        final String locator = "css=#foorz";
        final String xpath = "id('foorz')";
        final long maxRuntime = 4000;

        long start = TimerUtils.getTime();
        {
            assertNotElementPresent(locator);
            assertNotElementCount(locator, 5);
            assertNotXpathCount(xpath, 5);
            assertElementCount(locator, 0);
            assertXpathCount(xpath, 0);

            waitForNotElementPresent(locator);
            waitForNotElementCount(locator, 5);
            waitForNotXpathCount(xpath, 5);
            waitForElementCount(locator, 0);
            waitForXpathCount(xpath, 0);
        }
        long runtime = TimerUtils.getTime() - start;

        Assert.assertTrue(String.format("Test runtime (%d ms) exceeded maximum runtime (%d ms)", runtime, maxRuntime),
                          runtime <= maxRuntime);
    }
}
