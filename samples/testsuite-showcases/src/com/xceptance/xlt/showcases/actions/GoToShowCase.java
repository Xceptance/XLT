package com.xceptance.xlt.showcases.actions;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Go to the specified show case page
 */
public class GoToShowCase extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "GoToShowCase";

    /**
     * The name of the show case.
     */
    private final String anchorName;

    /**
     * Contains the corresponding anchor.
     */
    private HtmlAnchor anchor;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     * @param anchorName
     *            the name of the show case/anchor
     */
    public GoToShowCase(final AbstractHtmlPageAction previousAction, final String anchorName)
    {
        super(previousAction, TIMERNAME);
        this.anchorName = anchorName;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        // get the anchor
        anchor = getPreviousAction().getHtmlPage().getAnchorByName(anchorName);
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // load the page by click on the anchor
        loadPageByClick(anchor, 30000);
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {

        final HtmlPage page = getHtmlPage();
        // First, we check all common criteria. This code can be bundled and
        // reused
        // if needed. For the purpose of the programming example, we leave it
        // here as
        // detailed as possible.
        // We add a catch block to the test running.
        // Messages are logged.
        StandardValidator.getInstance().validate(page);

        // check if we are on the correct page
        Assert.assertTrue("Wrong Page!", page.getTitleText().contains(anchorName));
    }
}
