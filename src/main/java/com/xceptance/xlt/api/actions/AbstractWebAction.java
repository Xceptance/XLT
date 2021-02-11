/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.api.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.common.lang.StringUtils;
import com.xceptance.xlt.api.util.ResponseProcessor;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * AbstractWebAction is the base class for all HTTP-based actions. In order to perform requests, it relies on a web
 * client.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractWebAction extends AbstractAction
{
    /**
     * An empty request parameter list. Use together with {@link #createWebRequestSettings(URL, HttpMethod, List)}.
     */
    protected static final List<NameValuePair> EMPTY_PARAMETER_LIST = Collections.emptyList();

    /**
     * The web client doing all the HTTP stuff.
     */
    private XltWebClient webClient;

    /**
     * Creates a new AbstractWebAction object and gives it the passed timer name. This constructor is typically used for
     * an intermediate action in a sequence of actions, i.e. it has a previous action.
     * 
     * @param previousAction
     *            the action that preceded the current action
     * @param timerName
     *            the name of the timer that is associated with this action
     */
    protected AbstractWebAction(final AbstractWebAction previousAction, final String timerName)
    {
        super(previousAction, timerName);

        // use the same web client as the previous action if there was a previous action
        webClient = previousAction == null ? new XltWebClient() : (XltWebClient) previousAction.getWebClient();

        // tell the web client to record timer statistics under that name
        webClient.setTimerName(getTimerName());
    }

    /**
     * Creates a new AbstractWebAction object and gives it the passed timer name. This constructor is typically used for
     * the first action in a sequence of actions, i.e. it has no previous action.
     * 
     * @param timerName
     *            the name of the timer that is associated with this action
     */
    protected AbstractWebAction(final String timerName)
    {
        this(null, timerName);
    }

    /**
     * Registers the given response processor. Note that the processor is valid during the current action only.
     * 
     * @param processor
     *            the response processor
     */
    public void addResponseProcessor(final ResponseProcessor processor)
    {
        ((XltWebClient) getWebClient()).addResponseProcessor(processor);
    }

    /**
     * Closes the underlying web client and releases all resources associated with it. Usually, doing this explicitly is
     * not necessary since the framework takes care of this at the end of the test. However, if you create multiple
     * independent web sessions (by creating multiple {@link AbstractWebAction} objects without a previous action)
     * during a test, you might want to release any web session not needed any longer as soon as possible to reduce
     * over-all memory consumption. Note that accessing the web client afterwards will cause an
     * {@link IllegalStateException}.
     */
    public void closeWebClient()
    {
        // This can happen if this method is called more than once on the same action.
        if (webClient != null)
        {
            webClient.shutdown();
            webClient = null;
        }
    }

    /**
     * Returns the action that was passed as the previous action to the constructor. Allows access to data collected
     * during the previous action.
     * 
     * @return the previous action (may be null)
     */
    @Override
    public AbstractWebAction getPreviousAction()
    {
        return (AbstractWebAction) super.getPreviousAction();
    }

    /**
     * Returns the current web client. Makes it possible to perform low-level HTTP operations directly.
     * 
     * @return the current web client
     * @throws IllegalStateException
     *             if the web client has already been closed explicitly
     */
    public WebClient getWebClient()
    {
        if (webClient == null)
        {
            throw new IllegalStateException("Cannot access the web client any longer since it has been closed explicitly.");
        }

        return webClient;
    }

    /**
     * The run method must restore the state in case we constructed another action in between to prevalidate conditions.
     */
    @Override
    public void run() throws Throwable
    {
        // tell the web client to record timer statistics under that name
        ((XltWebClient) getWebClient()).setTimerName(getTimerName());

        try
        {
            super.run();
        }
        finally
        {
            ((XltWebClient) getWebClient()).clearResponseProcessors();
        }
    }

    /**
     * Creates a {@link WebRequest} object from the passed URL, request parameters and request method.
     * 
     * @param url
     *            the target URL
     * @param method
     *            the HTTP request method to be used
     * @param requestParameters
     *            the list of custom parameters to add
     * @throws MalformedURLException
     *             if an error occurred
     */
    protected WebRequest createWebRequestSettings(final URL url, final HttpMethod method, final List<NameValuePair> requestParameters)
        throws MalformedURLException
    {
        final WebRequest webRequest;

        // first we have to get rid of client url schema with &amp; in it
        String urlString = url.toString();
        urlString = StringUtils.replace(urlString, "&amp;", "&");
        URL newUrl = new URL(urlString);
        final boolean thereAreRequestParameters = requestParameters != null && !requestParameters.isEmpty();
        final boolean methodIsHttpPost = method == HttpMethod.POST;

        /*
         * For non-POST methods, simply setting the request parameters will _replace_ any existing query string in the
         * URL. So we simply append the custom parameters to the query string and do not use "setRequestParameters()".
         */
        if (thereAreRequestParameters && !methodIsHttpPost)
        {
            newUrl = buildNewUrl(newUrl, requestParameters);
        }

        webRequest = new WebRequest(newUrl, method);

        if (thereAreRequestParameters && methodIsHttpPost)
        {
            webRequest.setRequestParameters(requestParameters);
        }
        return webRequest;
    }

    private URL buildNewUrl(final URL newUrl, final List<NameValuePair> requestParameters) throws MalformedURLException
    {
        final String oldQueryString = newUrl.getQuery();
        final StringBuilder newQueryString = new StringBuilder();

        final boolean insertLeadingAmp = oldQueryString != null && !oldQueryString.isEmpty();

        if (insertLeadingAmp)
        {
            newQueryString.append(oldQueryString);
        }

        for (int index = 0; index < requestParameters.size(); index++)
        {
            final NameValuePair nameValuePair = requestParameters.get(index);
            /*
             * For each pair except the first, an & has to be appended. For the first pair it depends on whether there
             * is already a query (in which case insertLeadingAmp is true).
             */
            if (index > 0 || insertLeadingAmp)
            {
                newQueryString.append('&');
            }
            newQueryString.append(nameValuePair.getName()).append('=').append(nameValuePair.getValue());
        }

        return UrlUtils.getUrlWithNewQuery(newUrl, newQueryString.toString());
    }
}
