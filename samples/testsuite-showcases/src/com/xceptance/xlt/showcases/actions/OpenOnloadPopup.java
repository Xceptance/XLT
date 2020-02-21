package com.xceptance.xlt.showcases.actions;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Open the page with the onload popup and check if we get two windows.
 */
public class OpenOnloadPopup extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "OpenOnloadPopup";

    /**
     * The onload popup anchor.
     */
    private HtmlAnchor popupAnchor;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     */
    public OpenOnloadPopup(final AbstractHtmlPageAction previousAction)
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

        // get the link
        popupAnchor = page.getAnchorByName("onloadpopup");
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // now load the new page
        loadPageByClick(popupAnchor);
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

        // check if we get 2 windows
        Assert.assertTrue(getWebClient().getWebWindows().size() == 2);

        // validate the second page
        final HtmlPage popupPage = (HtmlPage) getWebClient().getWebWindowByName("onloadpopup").getEnclosedPage();

        StandardValidator.getInstance().validate(popupPage);

        // check if the popup page is correct
        Assert.assertEquals("onloadPopup", popupPage.getTitleText());

        // close popup
        getWebClient().deregisterWebWindow(getWebClient().getWebWindowByName("onloadpopup"));

        // check if we have only one window
        Assert.assertTrue(getWebClient().getWebWindows().size() == 1);
    }
}
