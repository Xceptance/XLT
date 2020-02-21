package com.xceptance.xlt.engine;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 */
public class SynchronousAjaxController extends AjaxController
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processSynchron(final HtmlPage page, final WebRequest webRequest, final boolean async)
    {
        return true;
    }
}
