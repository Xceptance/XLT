package com.xceptance.xlt.engine;

import java.net.URL;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * Opens a new page by loading the given URL.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class OpenPageAction extends AbstractHtmlPageAction
{
    /**
     * URL to load as string.
     */
    private final String urlString;

    /**
     * URL to load as URL object.
     */
    private URL url;

    /**
     * Constructor.
     * 
     * @param urlString
     *            URL to load as string
     */
    public OpenPageAction(final String urlString)
    {
        super(null);
        this.urlString = urlString;
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#execute()
     */
    @Override
    protected void execute() throws Exception
    {
        loadPage(url);
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#postValidate()
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();

        Assert.assertNotNull("Failed to load page for URL '" + url.toExternalForm() + "'.", page);
        Assert.assertEquals(200, page.getWebResponse().getStatusCode());
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#preValidate()
     */
    @Override
    public void preValidate() throws Exception
    {
        url = new URL(urlString);
    }

}
