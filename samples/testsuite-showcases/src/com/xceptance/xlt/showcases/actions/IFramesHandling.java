package com.xceptance.xlt.showcases.actions;

import java.util.List;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Click the link in the iframe.
 */
public class IFramesHandling extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "IFramesHandling";

    /**
     * the anchor
     */
    private HtmlAnchor anchor;

    /**
     * the name of the anchor for validation
     */
    private int depthOfIframes = 0;

    /**
     * Constructor
     * 
     * @param previousAction
     *            the action we come from
     */
    public IFramesHandling(final AbstractHtmlPageAction previousAction)
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
        HtmlPage page = getPreviousAction().getHtmlPage();

        // check if there are any frames
        Assert.assertFalse(page.getFrames().isEmpty());

        // run through the iframes to get the "last" iframe
        while (!page.getFrames().isEmpty())
        {
            page = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
            // increase depthOfIframes for post validation
            depthOfIframes++;
        }

        // collect the anchors of this page
        final List<HtmlAnchor> anchors = page.getAnchors();

        // check if we got at least one
        Assert.assertFalse(anchors.isEmpty());

        // we have enough anchors so get the correct
        anchor = anchors.get(0);
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // load the page by clicking the anchor
        loadPageByClick(anchor);
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {
        HtmlPage page = getHtmlPage();
        // First, we check all common criteria. This code can be bundled and
        // reused
        // if needed. For the purpose of the programming example, we leave it
        // here as
        // detailed as possible.
        // We add a catch block to the test running.
        // Messages are logged.
        StandardValidator.getInstance().validate(page);

        // check if we change the content successfully
        int i = 0;
        while (!page.getFrames().isEmpty())
        {
            page = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
            i++;
        }

        // now compare the depth before and after the action
        Assert.assertTrue(depthOfIframes + 1 == i);
    }
}
