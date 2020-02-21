package com.xceptance.xlt.showcases.actions;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;

/**
 * Push button and check effect of ajax request. Consider that this action could also use loadPageByClick.
 */
public class ExecuteAjax extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "ExecuteAjax";

    /**
     * The ajax button.
     */
    private HtmlButton ajaxButton;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     */
    public ExecuteAjax(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, TIMERNAME);
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // we need the ajax button
        ajaxButton = HtmlPageUtils.findSingleHtmlElementByXPath(page, "//button[@id='ajax']");

        // check if the content is correct
        Assert.assertTrue(page.getHtmlElementById("content").getTextContent().contains("empty"));
        Assert.assertFalse(page.getHtmlElementById("content").getTextContent().contains("foo bar baz bum"));

    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        // now push the button
        ajaxButton.click();

        // wait for content to be changed
        waitForContentChange(page);

        // set resulting page
        setHtmlPage(page);
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();

        // now check if we change the content
        Assert.assertTrue(page.getHtmlElementById("content").getTextContent().contains("foo bar baz bum"));
        Assert.assertFalse(page.getHtmlElementById("content").getTextContent().contains("empty"));
    }

    /**
     * Helper method which waits at most 30s for the content of the HTML element with ID &quot;content&quot; to change.
     * 
     * @param page
     *            the HTML page
     */
    private void waitForContentChange(final HtmlPage page)
    {
        final long end = System.currentTimeMillis() + 30000L;
        final String initialContent = HtmlPageUtils.findSingleHtmlElementByID(page, "content").getTextContent();
        while (System.currentTimeMillis() < end)
        {
            if (initialContent.equals(HtmlPageUtils.findSingleHtmlElementByID(page, "content").getTextContent()))
            {
                ThreadUtils.sleep(500L);
                continue;
            }
            break;
        }
    }
}
