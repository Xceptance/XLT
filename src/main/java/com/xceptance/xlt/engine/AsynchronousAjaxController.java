package com.xceptance.xlt.engine;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Ajax controller which forces all ajax calls to be performed asynchronously.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AsynchronousAjaxController extends AjaxController
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processSynchron(final HtmlPage page, final WebRequest request, final boolean async)
    {
        return false;
    }

}
