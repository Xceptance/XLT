/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.gargoylesoftware.css.dom.AbstractCSSRuleImpl;
import com.gargoylesoftware.css.dom.CSSImportRuleImpl;
import com.gargoylesoftware.css.dom.CSSMediaRuleImpl;
import com.gargoylesoftware.css.dom.CSSStyleRuleImpl;
import com.gargoylesoftware.css.parser.selector.Selector;
import com.gargoylesoftware.css.parser.selector.SelectorList;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersion.BrowserVersionBuilder;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.xpath.XPathHelper;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.css.CSSRule;
import com.gargoylesoftware.htmlunit.javascript.host.css.CSSRuleList;
import com.gargoylesoftware.htmlunit.javascript.host.css.CSSStyleSheet;
import com.gargoylesoftware.htmlunit.javascript.host.css.StyleSheetList;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.common.util.StringMatcher;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.SessionShutdownListener;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.ResponseProcessor;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.htmlunit.apache.XltApacheHttpWebConnection;
import com.xceptance.xlt.engine.htmlunit.okhttp3.OkHttp3WebConnection;
import com.xceptance.xlt.engine.socket.XltSockets;
import com.xceptance.xlt.engine.util.CssUtils;
import com.xceptance.xlt.engine.util.JSBeautifingResponseProcessor;
import com.xceptance.xlt.engine.util.LWPageUtilities;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * The {@link XltWebClient} class is an enhanced version of the HTMLUnit {@link WebClient} class. It hooks into the
 * WebClient to add some new functionality:
 * <ul>
 * <li>collect runtime statistics</li>
 * <li>optionally load all static resources referenced from a web page</li>
 * <li>optionally wait for a configurable amount of time between two requests</li>
 * <li>optionally simulate a browser's static content cache, i.e. static resources that have already been loaded, will
 * not be requested again</li>
 * </ul>
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XltWebClient extends WebClient implements SessionShutdownListener, AlertHandler
{
    /**
     * A regex pattern matching all allowed link types.
     */
    private static final String LINKTYPE_WHITELIST_PATTERN = "(?i)stylesheet|(fav|shortcut )?icon";

    /**
     * A regex pattern matching all allowed link media types.
     */
    private static final String LINK_MEDIA_WHITELIST_PATTERN = "(?i)screen|all";

    /**
     * The serialization id, not needed.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The global cache for compiled JS and CSS artifacts. The cache is shared among all {@link XltWebClient} instances.
     */
    private static final XltCache globalCache;

    /**
     * Holds the URLs and responses of static resources that have been loaded so far for a page.
     */
    private final ConcurrentHashMap<String, WebResponse> pageLocalCache = new ConcurrentHashMap<String, WebResponse>();

    /**
     * Enabled flag for static content loading.
     */
    private boolean loadStaticContent = false;

    /**
     * The name to use when storing request statistics.
     */
    private String timerName;

    /**
     * Whether the framework should throw an exception if an HTTP error occurred while loading a page.
     */
    private final boolean throwExcOnHttpErrorWhileLoadingPages;

    /**
     * Whether the framework should throw an exception if an HTTP error occurred while loading a resource embedded in a
     * page.
     */
    private final boolean throwExcOnHttpErrorWhileLoadingResources;

    /**
     * The request queue that manages the parallel download of resources.
     */
    private transient RequestQueue requestQueue;

    /**
     * The filter used to check whether or not a URL is to be loaded.
     */
    private final StringMatcher urlFilter;

    /**
     * The list of response processors.
     */
    private final List<ResponseProcessor> responseProcessors = new ArrayList<ResponseProcessor>();

    /**
     * JS Beautifying response processor (separate instance field since its (omni)presence is configuration-dependent).
     */
    private ResponseProcessor jsBeautifier;

    /**
     * Mode to use when loading static content referenced by CSS files.
     */
    private final CssMode cssMode;

    /**
     * Mapping of base URLs to relative CSS resource URLs.
     */
    private final ConcurrentHashMap<String, Collection<String>> cssResourceUrlCache = new ConcurrentHashMap<String, Collection<String>>();

    /**
     * The maximum number of download threads.
     */
    private int threadCount;

    /**
     * The JavaScript debugger
     */
    private final XltDebugger xltDebugger;

    // Initialize globally valid things.
    // This code assures, that we only do it once and reuse it all the time.
    static
    {
        XltSockets.initialize();

        final XltProperties props = XltProperties.getInstance();

        // setup the global cache
        final String logMsgFormat = "Specified size of %s cache is lower than the minimum size of '%d'. Will use the minimum size.";
        int jsCacheSize = props.getProperty("com.xceptance.xlt.js.cache.size", 100);
        if (jsCacheSize < ConcurrentLRUCache.MIN_SIZE)
        {
            if (XltLogger.runTimeLogger.isWarnEnabled())
            {
                XltLogger.runTimeLogger.warn(String.format(logMsgFormat, "JS", ConcurrentLRUCache.MIN_SIZE));
            }
        }
        jsCacheSize = Math.max(jsCacheSize, ConcurrentLRUCache.MIN_SIZE);

        int cssCacheSize = props.getProperty("com.xceptance.xlt.css.cache.size", 100);
        if (cssCacheSize < ConcurrentLRUCache.MIN_SIZE)
        {
            if (XltLogger.runTimeLogger.isWarnEnabled())
            {
                XltLogger.runTimeLogger.warn(String.format(logMsgFormat, "CSS", ConcurrentLRUCache.MIN_SIZE));
            }
        }
        cssCacheSize = Math.max(cssCacheSize, ConcurrentLRUCache.MIN_SIZE);

        globalCache = new XltCache(jsCacheSize, cssCacheSize);

        // configure the XPath engine to use
        final String xpathEngine = props.getProperty("com.xceptance.xlt.xpath.engine", "jaxen");
        XPathHelper.useJaxen = xpathEngine.equalsIgnoreCase("jaxen");
    }

    /**
     * Creates a new XltWebClient object. All settings are taken from the XLT configuration.
     */
    public XltWebClient()
    {
        this(null);
    }

    /**
     * Creates a new XltWebClient object that emulates the specified browser. All other settings are taken from the XLT
     * configuration.
     * 
     * @param browserVersion
     *            the browser version to use (may be <code>null</code>)
     */
    public XltWebClient(final BrowserVersion browserVersion)
    {
        super(copyAndModifyBrowserVersion(browserVersion));

        Session.getCurrent().addShutdownListener(this);

        final XltProperties props = XltProperties.getInstance();

        loadStaticContent = props.getProperty("com.xceptance.xlt.loadStaticContent", false);
        throwExcOnHttpErrorWhileLoadingPages = props.getProperty("com.xceptance.xlt.stopTestOnHttpErrors.page", true);
        throwExcOnHttpErrorWhileLoadingResources = props.getProperty("com.xceptance.xlt.stopTestOnHttpErrors.embedded", false);

        // configure the asynchronous download facility
        threadCount = props.getProperty("com.xceptance.xlt.staticContent.downloadThreads", 4);
        if (threadCount <= 0)
        {
            XltLogger.runTimeLogger.warn("Property 'com.xceptance.xlt.staticContent.downloadThreads' is set to an invalid value. Will use 1 instead.");
            threadCount = 1;
        }
        requestQueue = new RequestQueue(this, threadCount);

        /*
         * Configure the super class.
         */

        setAlertHandler(this);
        getOptions().setRedirectEnabled(true);
        getOptions().setThrowExceptionOnFailingStatusCode(false);
        getOptions().setPrintContentOnFailingStatusCode(false);
        getOptions().setHomePage(UrlUtils.ABOUT_BLANK);

        final int defaultTimeout = 10000;
        final int timeout = props.getProperty("com.xceptance.xlt.timeout", defaultTimeout);
        if (timeout < defaultTimeout)
        {
            XltLogger.runTimeLogger.warn("com.xceptance.xlt.timeout is set lower than " + Integer.toString(defaultTimeout) + "!");
        }
        getOptions().setTimeout(timeout);

        // CSS handling
        getOptions().setCssEnabled(props.getProperty("com.xceptance.xlt.cssEnabled", false));

        cssMode = CssMode.getMode(props.getProperty("com.xceptance.xlt.css.download.images"));

        // setup JavaScript engine
        int optimizationLevel = props.getProperty("com.xceptance.xlt.js.compiler.optimizationLevel", -1);
        if (optimizationLevel < -1 || optimizationLevel > 9)
        {
            XltLogger.runTimeLogger.warn("Property 'com.xceptance.xlt.js.compiler.optimizationLevel' is set to an invalid value. Will use -1 instead.");
            optimizationLevel = -1;
        }

        final boolean takeMeasurements = props.getProperty("com.xceptance.xlt.js.takeMeasurements", false);

        setJavaScriptEngine(new XltJavaScriptEngine(this, optimizationLevel, takeMeasurements));
        getOptions().setJavaScriptEnabled(props.getProperty("com.xceptance.xlt.javaScriptEnabled", false));
        getOptions().setThrowExceptionOnScriptError(props.getProperty("com.xceptance.xlt.stopTestOnJavaScriptErrors", false));

        // setup JavaScript debugger
        xltDebugger = new XltDebugger(this);
        if (props.getProperty("com.xceptance.xlt.js.debugger.enabled", false))
        {
            setJavaScriptDebuggerEnabled(true);

            // create JS beautifying response processor only when needed
            if (props.getProperty("com.xceptance.xlt.js.debugger.beautifyDownloadedJavaScript", true))
            {
                jsBeautifier = new JSBeautifingResponseProcessor();
            }
        }

        // default user authentication
        final String userName = props.getProperty("com.xceptance.xlt.auth.userName");
        if (userName != null && userName.length() > 0)
        {
            final String password = props.getProperty("com.xceptance.xlt.auth.password", "");

            ((DefaultCredentialsProvider) getCredentialsProvider()).addCredentials(userName, password);

            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Using credentials: " + userName + "/<password>");
            }
        }

        // proxy
        if (props.getProperty("com.xceptance.xlt.proxy", false))
        {
            final String proxyHost = props.getProperty("com.xceptance.xlt.proxy.host", "127.0.0.1");
            final int proxyPort = props.getProperty("com.xceptance.xlt.proxy.port", 8888);
            final String bypassForHosts = props.getProperty("com.xceptance.xlt.proxy.bypassForHosts", "");

            final String[] hostPatterns = StringUtils.split(bypassForHosts, " ,;");

            final ProxyConfig proxyConfig = new ProxyConfig(proxyHost, proxyPort, null);
            for (final String hostPattern : hostPatterns)
            {
                proxyConfig.addHostsToProxyBypass(hostPattern);
            }
            getOptions().setProxyConfig(proxyConfig);

            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Using proxy at " + proxyHost + ":" + proxyPort);
            }

            // proxy authentication
            final String proxyUserName = props.getProperty("com.xceptance.xlt.proxy.userName");
            final String proxyPassword = props.getProperty("com.xceptance.xlt.proxy.password");

            if (proxyUserName != null && proxyUserName.length() > 0)
            {
                ((DefaultCredentialsProvider) getCredentialsProvider()).addCredentials(proxyUserName, proxyPassword, proxyHost, proxyPort,
                                                                                       null);

                if (XltLogger.runTimeLogger.isInfoEnabled())
                {
                    XltLogger.runTimeLogger.info("Using proxy credentials: " + proxyUserName + "/<password>");
                }
            }
        }

        // setup AJAX controller
        final AjaxMode ajaxMode = AjaxMode.getMode(props.getProperty("com.xceptance.xlt.js.ajax.executionMode", "normal"));
        setAjaxController(getAjaxControllerForMode(ajaxMode));

        // setup JS/CSS cache
        setCache(globalCache);

        // build the URL filter
        final String includedUrls = props.getProperty("com.xceptance.xlt.http.filter.include", "");
        final String excludedUrls = props.getProperty("com.xceptance.xlt.http.filter.exclude", "");
        urlFilter = new StringMatcher(includedUrls, excludedUrls);

        // whether to put SSL into easy mode which allows to use invalid/self-signed certificates
        getOptions().setUseInsecureSSL(props.getProperty("com.xceptance.xlt.ssl.easyMode", false));

        // the SSL protocol (family) to use when in easy mode
        String easyModeProtocol = StringUtils.defaultIfBlank(props.getProperty("com.xceptance.xlt.ssl.easyModeProtocol"), "TLS");
        getOptions().setSSLInsecureProtocol(easyModeProtocol.trim());

        // the SSL handshake protocols to enable at an SSL socket
        final String sslProtocols = props.getProperty("com.xceptance.xlt.ssl.protocols");
        if (StringUtils.isNotBlank(sslProtocols))
        {
            getOptions().setSSLClientProtocols(StringUtils.split(sslProtocols, " ,;"));
        }

        // whether to use keep-alive connections
        if (!props.getProperty("com.xceptance.xlt.http.keepAlive", props.getProperty("com.xceptance.xlt.keepAlive", true)))
        {
            // add a "Connection: close" header to all requests
            addRequestHeader("Connection", "close");
        }

        // whether to use response compression
        if (!props.getProperty("com.xceptance.xlt.http.gzip", true))
        {
            // add an empty "Accept-Encoding" header to every request to overwrite HtmlUnit's defaults
            addRequestHeader("Accept-Encoding", "");
        }

        // browser history
        int historySizeLimit = props.getProperty("com.xceptance.xlt.browser.history.size", 1);
        if (historySizeLimit < 0)
        {
            XltLogger.runTimeLogger.warn("Property 'com.xceptance.xlt.browser.history.size' is set to an invalid value. Will use 1 instead.");
            historySizeLimit = 1;
        }
        getOptions().setHistorySizeLimit(historySizeLimit);

        // set web connection
        final WebConnection underlyingWebConnection;
        if (props.getProperty("com.xceptance.xlt.http.offline", false))
        {
            // we are in offline mode and return fixed responses
            underlyingWebConnection = new XltOfflineWebConnection();
        }
        else
        {
            final String client = props.getProperty("com.xceptance.xlt.http.client");
            if ("okhttp3".equals(client))
            {
                final boolean http2Enabled = props.getProperty("com.xceptance.xlt.http.client.okhttp3.http2Enabled", true);
                underlyingWebConnection = new OkHttp3WebConnection(this, http2Enabled);
            }
            else
            {
                // the default connection
                underlyingWebConnection = new XltApacheHttpWebConnection(this);
            }
        }

        XltLogger.runTimeLogger.debug("Using web connection class: " + underlyingWebConnection.getClass().getName());

        final XltHttpWebConnection connection = new XltHttpWebConnection(this, underlyingWebConnection);
        setWebConnection(connection);
    }

    /**
     * Returns the HTML-Unit page for the given request parameters. This method overrides the one from the super class
     * to add specific functionality like request logging.
     *
     * @param <P>
     *            any implementation of the interface {@link Page}
     * @param webWindow
     *            the web window
     * @param parameters
     *            the request parameters
     * @return the resulting page
     * @throws IOException
     *             thrown when an I/O error occurred.
     * @throws FailingHttpStatusCodeException
     *             thrown when processing the request failed
     */
    @Override
    @SuppressWarnings("unchecked")
    public <P extends Page> P getPage(final WebWindow webWindow, final WebRequest parameters)
        throws IOException, FailingHttpStatusCodeException
    {
        // special handling for "about:blank" URLs
        if (parameters.getUrl().toString().equals("about:blank"))
        {
            // bail out now, so this type of request will not show up in any
            // logs
            return (P) super.getPage(webWindow, parameters);
        }

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("Loading page from: " + parameters.getUrl());
        }

        if (webWindow.getTopWindow() == webWindow)
        {
            pageLocalCache.clear();
            PageStatistics.getPageStatistics().pageLoadStarted();
        }

        parameters.setDocumentRequest();
        Page page = null;

        // maintain the request stack
        final RequestStack requestStack = RequestStack.getCurrent();
        requestStack.setTimerName(getTimerName());

        try
        {
            requestStack.pushPage();

            try
            {
                page = super.getPage(webWindow, parameters);
                final WebResponse response = page.getWebResponse();

                if (response.getStatusCode() >= 500 && throwExcOnHttpErrorWhileLoadingPages)
                {
                    Session.getCurrent().setFailed(true);
                    throw new FailingHttpStatusCodeException(response);
                }

            }
            catch (final IOException e)
            {
                Session.getCurrent().setFailed(true);

                throw e;
            }
            catch (final ScriptException e)
            {
                // script is causing errors
                Session.getCurrent().setFailed(true);

                throw e;
            }

        }
        finally
        {
            requestStack.popPage();
        }

        return (P) page;
    }

    /**
     * Loads the content from the given URL and returns it as a string.
     *
     * @param url
     *            the URL to load
     * @return the content
     * @throws IOException
     *             thrown when the URL failed to load.
     * @throws FailingHttpStatusCodeException
     *             thrown when the HTTP response code is unequal to 200.
     */
    public LightWeightPage getLightWeightPage(final URL url) throws IOException, FailingHttpStatusCodeException
    {
        return getLightWeightPage(new WebRequest(url));
    }

    /**
     * Loads the content from the given URL and returns it as a string.
     *
     * @param webRequestSettings
     *            the request parameters
     * @return the content
     * @throws IOException
     *             thrown when the URL failed to load.
     * @throws FailingHttpStatusCodeException
     *             thrown when the HTTP response code is unequal to 200.
     */
    public LightWeightPage getLightWeightPage(final WebRequest webRequestSettings) throws IOException, FailingHttpStatusCodeException
    {
        final URL url = webRequestSettings.getUrl();

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("Loading page from: " + url);
        }

        final WebWindow window = getCurrentWindow();
        if (window == null || window.getTopWindow() == window)
        {
            pageLocalCache.clear();
            PageStatistics.getPageStatistics().pageLoadStarted();
        }

        webRequestSettings.setDocumentRequest();

        // maintain the request stack
        final RequestStack requestStack = RequestStack.getCurrent();
        requestStack.setTimerName(getTimerName());

        LightWeightPage page = null;
        WebResponse response = null;
        try
        {
            requestStack.pushPage();

            try
            {
                // get response
                response = loadWebResponse(webRequestSettings);

                // evaluate response status code -> throw exception if necessary
                if (response.getStatusCode() >= 500 && throwExcOnHttpErrorWhileLoadingPages)
                {
                    Session.getCurrent().setFailed(true);
                    throw new FailingHttpStatusCodeException(response);
                }

                // load static content if configured to do so
                loadStaticContent(response.getContentAsString(), response.getWebRequest().getUrl(), response.getContentCharset());

                page = new LightWeightPageImpl(response, getTimerName(), this);

            }
            catch (final IOException e)
            {
                Session.getCurrent().setFailed(true);

                throw e;
            }
        }
        finally
        {
            requestStack.popPage();
            if (window == null || window.getTopWindow() == window)
            {
                PageStatistics.getPageStatistics().pageLoadFinished();
            }
        }

        return page;
    }

    /**
     * Loads these static content resources on the given page that have not been loaded so far. This is useful only
     * after the page was modified via JavaScript.
     * <p>
     * Typically, static content is loaded right after loading the page. However, when using JavaScript, not always a
     * new page is loaded, but sometimes the old page is modified only. Here we have to take care that any static
     * content, which is new on the page, gets loaded as well.
     * <p>
     * After loading the resources for the given page, any child page (from frames and iframes) is recursively
     * processed.
     *
     * @param htmlPage
     *            the HTML page for which to load new content
     */
    public void loadNewStaticContent(final HtmlPage htmlPage)
    {
        if (loadStaticContent)
        {
            final URL referrerURL = htmlPage.getWebResponse().getWebRequest().getUrl();
            URL baseURL = referrerURL;
            List<?> elements = htmlPage.getByXPath("/html/head/base");
            if (!elements.isEmpty())
            {
                final String hrefAttribute = ((HtmlElement) elements.get(0)).getAttribute("href");
                if (StringUtils.isNotEmpty(hrefAttribute))
                {
                    final URL u = makeUrlAbsolute(baseURL, hrefAttribute);
                    if (u != null)
                    {
                        baseURL = u;
                    }
                }
            }

            if (getOptions().isJavaScriptEnabled())
            {
                final RequestStack requestStack = RequestStack.getCurrent();
                requestStack.setTimerName(getTimerName());

                final Set<String> urlStrings = new TreeSet<String>();
                // No need to select img elements too -> they are automatically downloaded (#1379)
                // Scripts can be skipped as well -> automatically downloaded as needed
                elements = htmlPage.getByXPath("//input[@type='image']");
                for (final Object o : elements)
                {
                    final String srcAttribute = ((HtmlElement) o).getAttribute("src");
                    if (StringUtils.isNotEmpty(srcAttribute))
                    {
                        urlStrings.add(srcAttribute);
                    }
                }

                elements = htmlPage.getByXPath("//link");
                for (final Object o : elements)
                {
                    final HtmlElement e = (HtmlElement) o;
                    final String relAttribute = e.getAttribute("rel");
                    final String mediaAttribute = e.getAttribute("media");
                    final String hrefAttribute = e.getAttribute("href");

                    if (StringUtils.isNotEmpty(hrefAttribute) && RegExUtils.isMatching(relAttribute, LINKTYPE_WHITELIST_PATTERN)
                        && (StringUtils.isBlank(mediaAttribute) || RegExUtils.isMatching(mediaAttribute, LINK_MEDIA_WHITELIST_PATTERN)))
                    {
                        urlStrings.add(hrefAttribute);
                    }
                }

                final StringBuilder sb = new StringBuilder();
                elements = htmlPage.getByXPath("//style");
                for (final Object o : elements)
                {
                    final HtmlElement e = (HtmlElement) o;
                    sb.append(e.getTextContent());
                }

                elements = htmlPage.getByXPath("//@style");
                for (final Object o : elements)
                {
                    sb.append(((DomAttr) o).getValue());
                }

                if (getCssMode().equals(CssMode.ALWAYS))
                {
                    urlStrings.addAll(CssUtils.getUrlStrings(sb.toString()));
                }
                else
                {
                    urlStrings.addAll(CssUtils.getRelativeImportUrlStrings(sb.toString()));
                }

                loadStaticContent(resolveUrls(urlStrings, baseURL), referrerURL, htmlPage.getCharset());
            }

            if (getOptions().isCssEnabled() && getCssMode().equals(CssMode.ONDEMAND))
            {
                evalCss(htmlPage, baseURL);
            }

            // use of copy of the frame list to minimize the chance of ConcurrentModificationException's (#1676)
            final List<FrameWindow> frames = new ArrayList<FrameWindow>(htmlPage.getFrames());
            for (final WebWindow frame : frames)
            {
                final Page framePage = frame.getEnclosedPage();
                if (framePage instanceof HtmlPage)
                {
                    loadNewStaticContent((HtmlPage) framePage);
                }
            }
        }
    }

    /**
     * Loads static content referenced from the given page represented as string. Currently, this includes:
     * <ul>
     * <li>images</li>
     * <li>style sheets</li>
     * <li>scripts</li>
     * </ul>
     *
     * @param page
     *            the unparsed source of the page
     * @param baseURL
     *            the URL that produced the page
     */
    private void loadStaticContent(String page, final URL baseURL, final Charset charset)
    {
        final boolean haveJS = getOptions().isJavaScriptEnabled();
        final boolean haveCss = getOptions().isCssEnabled();

        // Exit early
        if (page == null || (!loadStaticContent && !haveJS && !haveCss))
        {
            return;
        }

        // use a sorted set to hold the links -> this way each resource will be
        // loaded only once and in the same order
        final Set<String> urlStrings = new TreeSet<String>();

        final boolean isCssModeAlways = CssMode.ALWAYS.equals(getCssMode());

        // JS resources
        if (loadStaticContent || haveJS)
        {
            // remove comments (don't remove conditional comments if IE)
            final String commentPattern = getBrowserVersion().isIE() ? "(?sm)<!--[^\\[].*?-->" : "(?sm)<!--.*?-->";
            page = RegExUtils.replaceAll(page, commentPattern, "");

            // remove scripts (in-line scripts might contain resource URLs in the code)
            final Pattern p = RegExUtils.getPattern("(?sm)<script\\b(.*?)(?:/>|>.*?</script>)");
            final Matcher m = p.matcher(page);

            final StringBuilder sb = new StringBuilder();

            int startAt = 0;
            while (m.find(startAt))
            {
                final String matching = m.group(1);

                sb.append(page.substring(startAt, m.start()));
                final String srcAttribute = RegExUtils.getFirstMatch(matching, "src\\s*=\\s*['\"]([^'\"]+?)['\"]", 1);
                if (srcAttribute != null)
                {
                    urlStrings.add(srcAttribute);
                }
                startAt = m.end();
            }

            if (startAt < page.length())
            {
                sb.append(page.substring(startAt));
            }

            page = sb.toString();
        }

        // CSS resources
        if (loadStaticContent || haveCss)
        {
            urlStrings.addAll(getAllowedLinkURIs(page));

            final StringBuilder sb = new StringBuilder();
            final StringBuilder pageStringBuilder = new StringBuilder(4 * 1024);
            final Pattern p = RegExUtils.getPattern("(?sm)<style\\b([^>]*?)>((?!</style).*?)</style>");
            final Matcher m = p.matcher(page);

            int startAt = 0;
            while (m.find(startAt))
            {
                pageStringBuilder.append(page.substring(startAt, m.start()));
                final String styleAtts = m.group(1);
                // check type and media attributes if present
                final String typeAtt = LWPageUtilities.getAttributeValue(styleAtts, "type");
                final String mediaAtt = LWPageUtilities.getAttributeValue(styleAtts, "media");
                if ("text/css".equalsIgnoreCase(typeAtt)
                    && (StringUtils.isBlank(mediaAtt) || RegExUtils.isMatching(mediaAtt, LINK_MEDIA_WHITELIST_PATTERN)))
                {
                    sb.append(m.group(2));
                }

                startAt = m.end();
            }

            if (startAt < page.length())
            {
                pageStringBuilder.append(page.substring(startAt));
            }
            page = pageStringBuilder.toString();

            final String inlineCss = sb.toString();
            if (!isCssModeAlways || !loadStaticContent)
            {
                // only @import URL strings
                urlStrings.addAll(CssUtils.getRelativeImportUrlStrings(inlineCss));
            }
            else
            {
                // @import + image URL strings
                urlStrings.addAll(CssUtils.getUrlStrings(inlineCss));
                urlStrings.addAll(CssUtils.getUrlStrings(StringUtils.join(LWPageUtilities.getAllInlineCssStatements(page), ' ')));
            }

        }

        // image resources (not referenced by CSS)
        if (loadStaticContent)
        {
            urlStrings.addAll(LWPageUtilities.getAllImageLinks(page));
            urlStrings.addAll(LWPageUtilities.getAllImageInputLinks(page));
        }

        // check for base tag and correct base URL if necessary
        URL baseUrl = baseURL;
        final List<String> baseUrls = LWPageUtilities.getAllBaseLinks(page);
        if (!baseUrls.isEmpty())
        {
            final String hrefAttValue = baseUrls.get(0);
            final URL u = makeUrlAbsolute(baseUrl, hrefAttValue);
            if (u != null)
            {
                baseUrl = u;
            }
        }

        // finally load all collected resources
        loadStaticContent(resolveUrls(urlStrings, baseUrl), baseURL, charset);
    }

    /**
     * Loads static content referenced by the given URLs.
     *
     * @param urls
     *            list of URLs
     * @param referrerUrl
     *            the referrer URL
     */
    private void loadStaticContent(final Collection<URL> urls, final URL referrerUrl, final Charset charset)
    {
        // load resources asynchronously
        for (final URL url : urls)
        {
            requestQueue.addRequest(url, referrerUrl, charset);
        }

        requestQueue.waitForCompletion();
    }

    /**
     * Loads the content from the given URL.
     *
     * @param url
     *            the URL to load
     * @param url
     *            the referrer URL
     * @param charset
     *            the character set to use (aka document's character encoding)
     * @return the web response just loaded (may be <code>null</code>)
     * @throws IOException
     */
    WebResponse loadStaticContentFromUrl(final URL url, final URL referrerUrl, final Charset charset) throws IOException
    {
        try
        {
            final WebRequest webRequest = new WebRequest(url);
            if (referrerUrl != null)
            {
                webRequest.setAdditionalHeader("Referer", referrerUrl.toExternalForm());
            }

            webRequest.setCharset(charset);

            final WebResponse webResponse = loadWebResponse(webRequest);
            final int statusCode = webResponse.getStatusCode();
            if (statusCode >= 400)
            {
                // throw exception if configured to do so
                if (throwExcOnHttpErrorWhileLoadingResources)
                {
                    throw new FailingHttpStatusCodeException(webResponse);
                }
            }
            return webResponse;
        }
        catch (final IOException e)
        {
            // re-throw exception if configured to do so
            if (throwExcOnHttpErrorWhileLoadingResources)
            {
                throw e;
            }
            else
            {
                // the cause might be interesting to know so log it
                if (XltLogger.runTimeLogger.isWarnEnabled())
                {
                    XltLogger.runTimeLogger.warn("Failed to load static content from: " + url, e);
                }

                return null;
            }
        }
    }

    /**
     * {@inheritDoc} Any port specified in an URL that is equal to the protocol default port will be cleared. Responses
     * will be optionally cached if they are non-cachable since cachable responses are cached by the underlying caching
     * HTTP web connection.
     */
    @Override
    public WebResponse loadWebResponse(final WebRequest request) throws IOException
    {
        // handle default ports
        final URL pageURL = request.getUrl();
        final int port = pageURL.getPort();
        if (port >= 0 && port == pageURL.getDefaultPort())
        {
            request.setUrl(getUrlWithNewPort(pageURL, -1));
            request.setOriginalURL(pageURL);
        }

        final String urlString = request.getUrl().toString();
        WebResponse response = pageLocalCache.get(urlString);
        if (response == null)
        {
            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Loading " + urlString);
            }

            response = super.loadWebResponse(request);
            // XHRs are never cached
            // TODO: Check for POST request obsolete??
            if (!request.isXHR() && !request.isDocumentRequest() && request.getHttpMethod() != HttpMethod.POST)
            {
                pageLocalCache.put(urlString, response);
            }
        }

        if (response.getStatusCode() < 400)
        {
            handleCss(response, request.getCharset());
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page loadWebResponseInto(final WebResponse webResponse, final WebWindow webWindow)
        throws IOException, FailingHttpStatusCodeException
    {
        loadStaticContent(webResponse.getContentAsString(), webResponse.getWebRequest().getUrl(), webResponse.getContentCharset());

        final Page p = super.loadWebResponseInto(webResponse, webWindow);
        if (webWindow.getTopWindow() == webWindow)
        {
            PageStatistics.getPageStatistics().pageLoadFinished();
        }
        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void download(final WebWindow requestingWindow, final String target, final WebRequest request, final boolean checkHash,
                         final boolean forceLoad, final boolean forceAttachment, final String description)
    {
        if (requestingWindow.getTopWindow() == requestingWindow)
        {
            final URL requestedUrl = request.getUrl();
            final URL currentUrl = requestingWindow.getEnclosedPage().getWebResponse().getWebRequest().getUrl();

            // clear cache if it's not a hash jump
            final boolean isHashJump = requestedUrl.sameFile(currentUrl) && requestedUrl.getRef() != null;
            if (!isHashJump)
            {
                pageLocalCache.clear();
            }

            PageStatistics.getPageStatistics().pageLoadStarted();
        }

        request.setDocumentRequest();

        super.download(requestingWindow, target, request, checkHash, forceLoad, forceAttachment, description);
    }

    /**
     * Resolves the given collection of relative URL strings to absolute URLs using the given base URL.
     *
     * @param urlStrings
     *            relative URL strings
     * @param baseURL
     *            base URL
     * @return resolved URLs
     */
    private static Collection<URL> resolveUrls(final Collection<String> urlStrings, final URL baseURL)
    {
        // use a hash map to avoid URL duplicates since given collection of URL strings may also contain absolute URL
        // strings
        final HashMap<String, URL> resolvedURLs = new HashMap<String, URL>(urlStrings.size());
        for (final String urlString : urlStrings)
        {
            final URL absoluteURL = makeUrlAbsolute(baseURL, urlString);
            if (absoluteURL != null)
            {
                final String absoluteURLString = absoluteURL.toString();
                resolvedURLs.put(absoluteURLString, absoluteURL);
            }
        }

        return resolvedURLs.values();
    }

    /**
     * Returns all relative URLs used as 'href' attribute values in HTML link elements that match the link type
     * whitelist pattern.
     *
     * @param pageContent
     *            content of page
     * @return list of 'href' attribute values of allowed link elements
     */
    private List<String> getAllowedLinkURIs(final String pageContent)
    {
        final List<String> links = new ArrayList<String>();
        final String relAttRegex = loadStaticContent ? LINKTYPE_WHITELIST_PATTERN : "(?i)stylesheet";
        for (final String attributeList : LWPageUtilities.getAllLinkAttributes(pageContent))
        {
            // check for presence of 'rel' attribute
            final String relAttribute = LWPageUtilities.getAttributeValue(attributeList, "rel");
            if (relAttribute != null && RegExUtils.isMatching(relAttribute, relAttRegex))
            {
                final String hrefAttributeValue = LWPageUtilities.getAttributeValue(attributeList, "href");
                if (!StringUtils.isBlank(hrefAttributeValue))
                {
                    // check for presence of 'media' attribute
                    final String mediaAttribute = LWPageUtilities.getAttributeValue(attributeList, "media");
                    if (mediaAttribute == null || RegExUtils.isMatching(mediaAttribute, LINK_MEDIA_WHITELIST_PATTERN))
                    {
                        // link is valid -> add its 'href' attribute value to list
                        links.add(hrefAttributeValue);
                    }
                }
            }

        }
        return links;
    }

    /**
     * Shuts this web client instance down. This includes releasing any resources that may still be held, e.g.
     * keep-alive HTTP connections.
     */
    @Override
    public void shutdown()
    {
        // set a timer name for shutdown activities
        setTimerName("BrowserShutdown");

        // shutdown JS engine, close all top-level windows and shutdown web connection
        close();

        // shutdown the rest
        requestQueue.shutdown();
        pageLocalCache.clear();
    }

    /**
     * Returns the timer name used for the next web request.
     *
     * @return the timerName
     */
    public String getTimerName()
    {
        return timerName;
    }

    /**
     * Sets the timer name to use for the next web request.
     *
     * @param timerName
     *            the timerName to set
     */
    public void setTimerName(final String timerName)
    {
        this.timerName = timerName;
        RequestStack.getCurrent().reset();
        RequestStack.getCurrent().setTimerName(timerName);
    }

    /**
     * Checks whether the passed URL is OK to be loaded.
     *
     * @param url
     *            the URL to check
     * @return whether the URL is to be loaded
     */
    public boolean isAcceptedUrl(final URL url)
    {
        return urlFilter.isAccepted(url.toString());
    }

    /**
     * Modifies the given web response using the registered response processors. Note that the processors are called in
     * the same order as they have been registered.
     *
     * @param webResponse
     *            the web response
     * @return the (potentially) modified web response
     */
    public WebResponse processResponse(final WebResponse webResponse)
    {
        WebResponse response = webResponse;
        for (int i = 0; i < responseProcessors.size(); i++)
        {
            response = responseProcessors.get(i).processResponse(response);
        }
        // process response using the JS Beautifier if present
        if (jsBeautifier != null && isJavaScriptDebuggerEnabled())
        {
            response = jsBeautifier.processResponse(response);
        }

        return response;
    }

    /**
     * Clears the current list of response processors.
     */
    public void clearResponseProcessors()
    {
        responseProcessors.clear();
    }

    /**
     * Registers the given response processor.
     *
     * @param processor
     *            the response processor
     */
    public void addResponseProcessor(final ResponseProcessor processor)
    {
        responseProcessors.add(processor);
    }

    /**
     * Unregisters the given response processor.
     *
     * @param processor
     *            the response processor
     */
    public void removeResponseProcessor(final ResponseProcessor processor)
    {
        responseProcessors.remove(processor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAlert(final Page page, final String message)
    {
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("window.alert() message: " + message);
        }
    }

    /**
     * Waits for all background threads operating on the given page to finish, but does not wait longer than the given
     * waiting time.
     *
     * @param page
     *            the page to check
     * @param maximumWaitingTime
     *            the maximum time (in milliseconds) to wait
     */
    public void waitForBackgroundThreads(final Page page, final long maximumWaitingTime)
    {
        if (getOptions().isJavaScriptEnabled())
        {
            if (maximumWaitingTime >= 0)
            {
                if (XltLogger.runTimeLogger.isDebugEnabled())
                {
                    XltLogger.runTimeLogger.debug(String.format("Waiting at most %d ms for all background jobs in all web windows.",
                                                                maximumWaitingTime));
                }

                final long end = TimerUtils.getTime() + maximumWaitingTime;

                // first determine all web windows - note that this is *not safe* if
                // more web windows are added later by background JavaScript
                final List<WebWindow> webWindows = getAllWebWindows(page);

                // now wait for each web window's threads
                for (final WebWindow webWindow : webWindows)
                {
                    final JavaScriptJobManager jobManager = webWindow.getJobManager();
                    if (jobManager != null)
                    {
                        // wait for at most the remaining time for running jobs to
                        // complete
                        final int remainingJobs;
                        final long remainingWaitingTime = end - TimerUtils.getTime();
                        if (remainingWaitingTime > 0)
                        {
                            if (XltLogger.runTimeLogger.isDebugEnabled())
                            {
                                XltLogger.runTimeLogger.debug(String.format("Now waiting for background jobs in web window '%s'.",
                                                                            webWindow.toString()));
                            }

                            remainingJobs = jobManager.waitForJobs(remainingWaitingTime);
                        }
                        else
                        {
                            remainingJobs = jobManager.getJobCount();
                        }

                        if (remainingJobs > 0)
                        {
                            // there are still background activities :-(
                            if (XltLogger.runTimeLogger.isWarnEnabled())
                            {
                                XltLogger.runTimeLogger.warn(String.format("%d background job(s) still running in web window '%s'. Check your JavaScript code. You may also increase the waiting time (currently: %d ms).",
                                                                           remainingJobs, webWindow.toString(), maximumWaitingTime));
                            }
                        }

                        // remove all remaining background jobs
                        jobManager.removeAllJobs();
                    }
                }
            }
            else
            {
                // in case of negative waiting time, do NOT wait for background jobs, nor kill them
                XltLogger.runTimeLogger.debug("Will not wait for running background jobs to finish, nor kill pending jobs.");
            }
        }
    }

    /**
     * Returns the CSS image download mode.
     *
     * @return CSS image download mode
     */
    public CssMode getCssMode()
    {
        return cssMode;
    }

    /**
     * Returns whether or not download of static content is enabled.
     *
     * @return <code>true</code> if download of static content is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isLoadStaticContent()
    {
        return loadStaticContent;
    }

    /**
     * Sets whether or not to load static content.
     *
     * @param loadStaticContent
     *            whether or not to load static content
     */
    public void setLoadStaticContent(final boolean loadStaticContent)
    {
        this.loadStaticContent = loadStaticContent;
    }

    /**
     * Caches all relative CSS resource URLs contained in the given CSS response.
     *
     * @param r
     *            CSS response to cache the relative resource URLs for
     */
    private void cacheResourceUrlsOfCssResponse(final WebResponse r)
    {
        URL url = r.getWebRequest().getUrl();
        try
        {
            url = normalizeUrl(url);
        }
        catch (final MalformedURLException mue)
        {
            if (XltLogger.runTimeLogger.isWarnEnabled())
            {
                XltLogger.runTimeLogger.warn("Failed to normalize URL '" + url + "'");
            }
        }

        final String requestUrlString = url.toString();

        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Caching resource URLs of CSS response for URL: " + requestUrlString);
        }

        if (!cssResourceUrlCache.containsKey(requestUrlString))
        {
            cssResourceUrlCache.put(requestUrlString, CssUtils.getUrlStrings(CssUtils.clearImportRules(r)));
        }
    }

    /**
     * CSS response handling.
     *
     * @param response
     *            the response to be handled
     */
    private void handleCss(final WebResponse response, final Charset charset)
    {
        if (!CssUtils.isCssResponse(response))
        {
            return;
        }

        final Set<String> urlStrings;
        final CssMode cssMode = getCssMode();
        final String cssContent = response.getContentAsString();
        if (loadStaticContent && cssMode.equals(CssMode.ALWAYS))
        {
            urlStrings = CssUtils.getUrlStrings(cssContent);
        }
        else
        {
            if (cssMode.equals(CssMode.ONDEMAND))
            {
                cacheResourceUrlsOfCssResponse(response);
            }

            // in any case download at least imported CSS files
            urlStrings = CssUtils.getRelativeImportUrlStrings(cssContent);
        }

        for (final String urlString : urlStrings)
        {
            final URL url = makeUrlAbsolute(response.getWebRequest().getUrl(), urlString);
            if (url != null)
            {
                final URL referrerUrl = response.getWebRequest().getUrl();
                requestQueue.addRequest(url, referrerUrl, charset);
            }
        }
    }

    /**
     * Evaluates the actual CSS style for all elements of the given page.
     *
     * @param page
     *            the HTML page
     * @param baseURL
     *            the URL to use for resolution of relative URLs
     */
    private void evalCss(final HtmlPage page, final URL baseURL)
    {
        if (page == null || page.getBody() == null)
        {
            return;
        }

        // collect the combined CSS text for all elements on the page that directly (via style attribute) or indirectly
        // (via class attribute) reference image resources
        final HtmlElement body = page.getBody();
        final List<CSSStyleRuleImpl> cssRulesWithUrls = getCssRulesWithUrls(page);
        final StringBuilder cssText = new StringBuilder(4096);
        final BrowserVersion browserVersion = page.getWebClient().getBrowserVersion();

        evalCss(body, cssRulesWithUrls, browserVersion, cssText);

        // create absolute URLs
        final Collection<URL> urls = new ArrayList<URL>();
        final Set<String> alreadyLoadedUrls = new HashSet<String>(pageLocalCache.keySet());
        final List<URL> pageBaseURLs = Arrays.asList(baseURL);

        for (final String urlString : CssUtils.getUrlStrings(cssText.toString()))
        {
            List<URL> baseURLs = getCssBaseUrls(urlString);
            if (baseURLs.isEmpty())
            {
                baseURLs = pageBaseURLs;
            }

            for (final URL u : baseURLs)
            {
                final URL url = makeUrlAbsolute(u, urlString);
                if (url != null && !alreadyLoadedUrls.contains(url.toString()))
                {
                    urls.add(url);
                    alreadyLoadedUrls.add(url.toString());
                }
            }
        }

        // load content referred by collected URLs
        // TODO: If a resource is referenced from a CSS file, we should use that file's URL as referrer URL, not the
        // page's URL.
        final URL referrerUrl = page.getWebResponse().getWebRequest().getUrl();
        loadStaticContent(urls, referrerUrl, page.getCharset());
    }

    /**
     * Returns the base URL for the given relative CSS resource URL string.
     *
     * @param urlString
     *            relative CSS resource URL string
     * @return base URL of given relative CSS resource URL string
     */
    private List<URL> getCssBaseUrls(final String urlString)
    {
        final List<URL> urls = new ArrayList<URL>();
        for (final Map.Entry<String, Collection<String>> entry : cssResourceUrlCache.entrySet())
        {
            if (entry.getValue().contains(urlString))
            {
                try
                {
                    urls.add(new URL(entry.getKey()));
                }
                catch (final MalformedURLException e)
                {
                    if (XltLogger.runTimeLogger.isWarnEnabled())
                    {
                        XltLogger.runTimeLogger.warn("Failed to create URL from: " + entry.getKey(), e);
                    }
                }
            }
        }

        return urls;
    }

    /**
     * Checks the given element whether it directly (via a style attribute) or indirectly (via a class attribute)
     * references image resources. If it does, the respective CSS style definition is appended to the specified CSS text
     * string builder for further processing later on. Finally, the method is recursively called for all child elements.
     *
     * @param element
     *            the element
     * @param cssRules
     *            the list of CSS rules to check
     * @param browserVersion
     *            the browser version
     * @param cssText
     *            the resulting CSS style definition with all the URLs
     */
    private void evalCss(final HtmlElement element, final List<CSSStyleRuleImpl> cssRules, final BrowserVersion browserVersion,
                         final StringBuilder cssText)
    {
        // first remember the style defined directly at the element, but only if it references an image
        final String elementStyle = element.getAttribute("style");
        if (containsUrl(elementStyle))
        {
            cssText.append(elementStyle);
        }

        // now check all CSS rules
        final Iterator<CSSStyleRuleImpl> rulesIterator = cssRules.iterator();
        while (rulesIterator.hasNext())
        {
            final CSSStyleRuleImpl rule = rulesIterator.next();

            // try each of the rule's selectors with the element
            final SelectorList selectors = rule.getSelectors();
            for (int j = 0; j < selectors.size(); j++)
            {
                // check whether the selector matches the element
                final Selector selector = selectors.get(j);
                final boolean selected = CSSStyleSheet.selects(browserVersion, selector, element, null, false);
                if (selected)
                {
                    // the rule applied to this element -> remember the rule's style definition
                    cssText.append(rule.getCssText());

                    // the rule has been processed -> remove it, no need to try this rule again and again
                    rulesIterator.remove();

                    // no need to try any other selector for this rule
                    break;
                }
            }
        }

        // recursively collect any style with URLs
        for (final DomElement childElement : element.getChildElements())
        {
            if (childElement instanceof HtmlElement)
            {
                evalCss((HtmlElement) childElement, cssRules, browserVersion, cssText);
            }
        }
    }

    /**
     * Scans all the style sheets associated with the given page for CSS style rules that reference an image and returns
     * the matching CSS rules.
     *
     * @param page
     *            the HTML page to check
     * @return the list of CSS rules found
     */
    private List<CSSStyleRuleImpl> getCssRulesWithUrls(final HtmlPage page)
    {
        final List<CSSStyleRuleImpl> cssRules = new ArrayList<CSSStyleRuleImpl>();

        final Window window = ((Window) page.getEnclosingWindow().getScriptableObject());
        final HTMLDocument document = ((HTMLDocument) window.getDocument());

        // check all style sheets
        final StyleSheetList sheets = document.getStyleSheets();
        for (int i = 0; i < sheets.getLength(); i++)
        {
            final CSSStyleSheet sheet = (CSSStyleSheet) sheets.item(i);
            addCssStyleRulesWithUrls(sheet, cssRules);
        }

        return cssRules;
    }

    /**
     * Scans the given style sheet for CSS style rules that reference an image and returns the matching CSS rules.
     *
     * @param sheet
     *            the CSS style sheet to process
     * @param cssRules
     *            the list of CSS style rules that is filled with the found style rules that might reference an image
     */
    private void addCssStyleRulesWithUrls(final CSSStyleSheet sheet, final List<CSSStyleRuleImpl> cssRules)
    {
        // only active sheets (with no or "screen" media type) are of interest
        if (sheet.isActive())
        {
            // check all CSS rules
            final CSSRuleList rules = sheet.getCssRules();
            for (int r = 0; r < rules.getLength(); r++)
            {
                try
                {
                    final CSSRule rule = (CSSRule) rules.get(r, null);
                    addCssStyleRulesWithUrls(sheet, rule.getRule(), cssRules);
                }
                catch (final Throwable t)
                {
                    if (XltLogger.runTimeLogger.isDebugEnabled())
                    {
                        XltLogger.runTimeLogger.debug("Failed to process CSS style sheet: " + sheet.getUri() + ". Cause: "
                                                      + t.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Scans the given CSS rule associated with the style sheet for CSS style rules that reference an image and returns
     * the matching CSS rules.
     *
     * @param sheet
     *            the CSS style sheet to process
     * @param rule
     *            the CSS rule to inspect
     * @param cssStyleRules
     *            the list of CSS style rules that is filled with the found style rules that might reference an image
     */
    private void addCssStyleRulesWithUrls(final CSSStyleSheet sheet, final AbstractCSSRuleImpl rule,
                                          final List<CSSStyleRuleImpl> cssStyleRules)
    {
        // remember the rule only if it is a style rule and if it references images
        if (rule instanceof CSSStyleRuleImpl && containsUrl(rule.getCssText()))
        {
            if (containsUrl(rule.getCssText()))
            {
                cssStyleRules.add((CSSStyleRuleImpl) rule);
            }
        }
        // recursively process imported style sheets
        else if (rule instanceof CSSImportRuleImpl)
        {
            final CSSImportRuleImpl importRule = (CSSImportRuleImpl) rule;

            // get and check media text
            final String mediaText = importRule.getMedia().getMediaText();
            if (StringUtils.isBlank(mediaText) || RegExUtils.isMatching(mediaText, LINK_MEDIA_WHITELIST_PATTERN))
            {
                // construct absolute URL string
                final String urlString = UrlUtils.resolveUrl(sheet.getUri(), importRule.getHref());

                // load imported style sheet
                final CSSStyleSheet importedSheet = CSSStyleSheet.loadStylesheet(sheet.getOwnerNode(), null, urlString);

                // recurse into imported style sheet if there is one
                if (importedSheet != null)
                {
                    addCssStyleRulesWithUrls(importedSheet, cssStyleRules);
                }
            }
        }
        // media rules might contain additional style rules
        else if (rule instanceof CSSMediaRuleImpl)
        {
            final CSSMediaRuleImpl mediaRule = (CSSMediaRuleImpl) rule;

            // get the media type of the rule and check if it is allowed ('all', 'screen' or blank)
            final String mediaText = mediaRule.getMediaList().getMediaText();
            if (StringUtils.isBlank(mediaText) || RegExUtils.isMatching(mediaText, LINK_MEDIA_WHITELIST_PATTERN))
            {
                // get the embedded CSS rules and process them recursively
                final List<AbstractCSSRuleImpl> rules = mediaRule.getCssRules().getRules();
                for (AbstractCSSRuleImpl r : rules)
                {
                    addCssStyleRulesWithUrls(sheet, r, cssStyleRules);
                }
            }
        }
    }

    /**
     * Checks whether the passed CSS style definition contains a URL.
     *
     * @param cssText
     *            the style definition to check
     * @return whether the style contains a URL
     */
    private static boolean containsUrl(final String cssText)
    {
        return cssText.toLowerCase().contains("url(");
    }

    /**
     * Recursively determines all web windows on the given page.
     *
     * @param page
     *            the page to check
     * @return a list of all web windows found
     */
    private List<WebWindow> getAllWebWindows(final Page page)
    {
        final List<WebWindow> webWindows = new ArrayList<WebWindow>();

        if (page != null)
        {
            // first add the page's web window
            webWindows.add(page.getEnclosingWindow());

            // now add the child web windows
            if (page instanceof HtmlPage)
            {
                final List<FrameWindow> frames = ((HtmlPage) page).getFrames();
                for (final FrameWindow frameWindow : frames)
                {
                    webWindows.addAll(getAllWebWindows(frameWindow.getEnclosedPage()));
                }
            }
        }

        return webWindows;
    }

    /**
     * Returns the absolute URL for the given base URL and relative URL. If the given relative URL is already absolute
     * it will stay unchanged.
     *
     * @param baseURL
     *            base URL
     * @param relativeUrl
     *            relative URL
     * @return resolved absolute URL
     */
    public static URL makeUrlAbsolute(final URL baseURL, String relativeUrl)
    {
        if (baseURL == null || relativeUrl == null || relativeUrl.trim().length() == 0)
        {
            return null;
        }

        relativeUrl = StringEscapeUtils.unescapeHtml4(relativeUrl);
        try
        {
            final String urlString = relativeUrl.startsWith("http") ? relativeUrl : UrlUtils.resolveUrl(baseURL, relativeUrl);
            return normalizeUrl(new URL(urlString));
        }
        catch (final MalformedURLException mue)
        {
            if (XltLogger.runTimeLogger.isWarnEnabled())
            {
                final String errMsg = String.format("Cannot create new URL from base URL '%s' and relative URL '%s'", baseURL, relativeUrl);
                XltLogger.runTimeLogger.warn(errMsg);
            }
        }

        return null;
    }

    /**
     * Activates or deactivates the JavaScript debugger.
     *
     * @param enabled
     *            set <code>true</code> to activate JavaScript debugger, <code>false</code> otherwise
     */
    public void setJavaScriptDebuggerEnabled(final boolean enabled)
    {
        xltDebugger.setEnabled(enabled);
    }

    /**
     * Determines whether JavaScript debugger is active or not.
     *
     * @return {@link Boolean}
     */
    public boolean isJavaScriptDebuggerEnabled()
    {
        return xltDebugger.isEnabled();
    }

    private static URL normalizeUrl(final URL url) throws MalformedURLException
    {
        final int port = url.getPort();
        if (port >= 0 && port == url.getDefaultPort())
        {
            return getUrlWithNewPort(url, -1);
        }

        return url;
    }

    /**
     * Creates a new URL based on the given URL but using the given port.
     *
     * @param url
     *            original URL
     * @param port
     * @return new URL which is the same as the given one except that the given port is used
     * @throws MalformedURLException
     */
    private static URL getUrlWithNewPort(final URL url, final int port) throws MalformedURLException
    {
        final String path = url.getPath();
        final String query = url.getQuery();
        final String ref = url.getRef();
        final String userInfo = url.getUserInfo();

        final StringBuilder s = new StringBuilder();
        s.append(url.getProtocol());
        s.append("://");

        if (userInfo != null && userInfo.length() > 0)
        {
            s.append(userInfo);
            s.append("@");
        }

        s.append(url.getHost());
        if (port != -1)
        {
            s.append(":").append(port);
        }

        if (path != null && path.length() > 0)
        {
            if (path.charAt(0) != '/')
            {
                s.append("/");
            }
            s.append(path);
        }

        if (query != null)
        {
            s.append("?").append(query);
        }
        if (ref != null)
        {
            if (ref.charAt(0) != '#')
            {
                s.append("#");
            }
            s.append(ref);
        }

        return new URL(s.toString());
    }

    private static AjaxController getAjaxControllerForMode(final AjaxMode ajaxMode)
    {
        switch (ajaxMode)
        {
            case RESYNC:
                return new NicelyResynchronizingAjaxController();
            case NORMAL:
                return new AjaxController();
            case ASYNC:
                return new AsynchronousAjaxController();
            default:
                return new SynchronousAjaxController();
        }
    }

    /**
     * Returns a copy of the given browser version, the copy modified according to the configuration. If the passed
     * browser version is <code>null</code>, the browser version to be copied will be determined from the configuration
     * as well.
     * 
     * @param browserVersion
     *            the base browser version (maybe <code>null</code>)
     * @return the modified browser version
     */
    private static BrowserVersion copyAndModifyBrowserVersion(BrowserVersion browserVersion)
    {
        // if null, use the browser version as given in the configuration
        if (browserVersion == null)
        {
            browserVersion = determineBrowserVersion();
        }

        // create a builder to build a modified version of the passed browser version
        final BrowserVersionBuilder browserVersionBuilder = new BrowserVersionBuilder(browserVersion);

        // modify user agent
        String customUserAgent = XltProperties.getInstance().getProperty("com.xceptance.xlt.browser.userAgent", "");
        if (customUserAgent.length() == 0)
        {
            // no special user agent defined so take the default one, but extend it with XLT
            final ProductInformation productInfo = ProductInformation.getProductInformation();
            final String productIdentifier = productInfo.getProductName() + " " + productInfo.getVersion();
            customUserAgent = browserVersion.getUserAgent().replace(")", "; " + productIdentifier + ")");
        }

        browserVersionBuilder.setUserAgent(customUserAgent);

        // finally build the modified browser version
        return browserVersionBuilder.build();
    }

    /**
     * Determines the configured browser version.
     *
     * @return the browser version
     */
    private static BrowserVersion determineBrowserVersion()
    {
        final BrowserVersion browserVersion;
        final String browserType = XltProperties.getInstance().getProperty("com.xceptance.xlt.browser", "FF").toUpperCase();

        if (browserType.equals("IE"))
        {
            browserVersion = BrowserVersion.INTERNET_EXPLORER;
        }
        else if (browserType.equals("CH"))
        {
            browserVersion = BrowserVersion.CHROME;
        }
        else if (browserType.equals("EDGE"))
        {
            browserVersion = BrowserVersion.EDGE;
        }
        else if (browserType.equals("FF_ESR"))
        {
            browserVersion = BrowserVersion.FIREFOX_78;
        }
        else
        {
            // "FF"
            browserVersion = BrowserVersion.FIREFOX;
        }

        return browserVersion;
    }

    enum CssMode
    {
     /**
      * Always download images references in CSS files.
      */
     ALWAYS,

     /**
      * Resolve and download image references on demand.
      */
     ONDEMAND,

     /**
      * Never resolve or download any image reference.
      */
     NEVER;

        /**
         * Returns the CSSMode instance for the given mode string.
         *
         * @param modeString
         *            mode string
         * @return corresponding CSSMode instance if any, or {@link CssMode#NEVER}.
         */
        static CssMode getMode(final String modeString)
        {
            CssMode mode = CssMode.NEVER;
            if (modeString != null && modeString.trim().length() > 0)
            {
                final String s = modeString.toLowerCase().trim();
                if (s.equals("always"))
                {
                    mode = CssMode.ALWAYS;
                }
                else if (s.equals("ondemand"))
                {
                    mode = CssMode.ONDEMAND;
                }
            }

            return mode;
        }
    }

    enum AjaxMode
    {
     /**
      * Perform AJAX calls always asynchronously.
      */
     ASYNC,

     /**
      * Perform AJAX calls always synchronously.
      */
     SYNC,

     /**
      * Perform AJAX calls as intended by programmer.
      */
     NORMAL,

     /**
      * Re-synchronize asynchronous AJAX calls calling from the main thread.
      */
     RESYNC;

        /**
         * Returns the AjaxMode instance for the given mode string.
         *
         * @param modeString
         *            mode string
         * @return corresponding AjaxMode if any, or {@link AjaxMode#SYNC}
         */
        static AjaxMode getMode(final String modeString)
        {
            AjaxMode mode = NORMAL;
            if (modeString != null && modeString.length() > 0)
            {
                final String s = modeString.toLowerCase().trim();
                if (s.equals("async"))
                {
                    mode = ASYNC;
                }
                else if (s.equals("sync"))
                {
                    mode = SYNC;
                }
                else if (s.equals("resync"))
                {
                    mode = RESYNC;
                }
            }
            return mode;
        }
    }
}
