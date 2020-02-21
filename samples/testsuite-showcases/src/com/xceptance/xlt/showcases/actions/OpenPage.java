package com.xceptance.xlt.showcases.actions;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Loads the page. And check for provided conditions (requiredText & disallowedText)
 */
public class OpenPage extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "OpenPage";

    /**
     * The URL string to fetch the data from.
     */
    private final String urlAsString;

    /**
     * contains the referrer of the page this is necessary because we don't have a valid flow
     */
    private final String referrer;

    /**
     * The URL object to fetch the data from.
     */
    private URL url;

    /**
     * Each page has to contain this text terms
     */
    private final List<String> requiredText;

    /**
     * This text pattern are forbidden for each page
     */
    private final List<String> disallowedText;

    /**
     * XSSAttack providing the URL as string.
     * 
     * @param urlAsString
     *            the URL to fetch the data from
     */
    public OpenPage(final String urlAsString)
    {
        this(null, urlAsString, "", new LinkedList<String>(), new LinkedList<String>());
    }

    /**
     * XSSAttack providing the URL as string and the referrer as String (for logging).
     * 
     * @param urlAsString
     *            the URL to fetch the data from
     * @param referrer
     *            the referrer from the page
     * @param required
     *            required text patterns on new page
     * @param disallowed
     *            disallowed text patterns on new page
     */
    public OpenPage(final AbstractHtmlPageAction previousAction, final String urlAsString, final String referrer,
                    final List<String> required, final List<String> disallowed)
    {
        super(previousAction, TIMERNAME);
        this.urlAsString = urlAsString;
        this.referrer = referrer;
        requiredText = required;
        disallowedText = disallowed;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        // we have to check, whether or not the passed url is valid
        Assert.assertNotNull("Url must not be null", urlAsString);

        // use the java URL class to do the final validation, it will
        // throw an exception, if this is not a valid url. We do not
        // have to deal with the exception, the framework will do it.
        url = new URL(urlAsString);
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // disable the redirect to check http status
        // getWebClient().setRedirectEnabled(false);

        // load the page simply by firing the url
        // always make sure that loadPage* methods are used
        loadPage(url);
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
        try
        {
            StandardValidator.getInstance().validate(page);
        }
        catch (final Throwable e)
        {
            final String message = page.getWebResponse().getWebRequest().getUrl() + "" + e + " referrer: " + referrer;
            Logger.getLogger("crawler").error(message);
        }

        // check for required text
        for (final String required : requiredText)
        {
            if (!page.getWebResponse().getContentAsString().contains(required))
            {
                final String message = page.getWebResponse().getWebRequest().getUrl() + " doesn't contain required term " + required +
                                       " referrer: " + referrer;
                Logger.getLogger("crawler").error(message);
            }
        }

        // check for disallowed text
        for (final String disallowed : disallowedText)
        {
            if (page.getWebResponse().getContentAsString().contains(disallowed))
            {
                final String message = page.getWebResponse().getWebRequest().getUrl() + " contain disallowed term " + disallowed +
                                       " referrer: " + referrer;
                Logger.getLogger("crawler").error(message);
            }
        }
    }
}
