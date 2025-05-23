/*
 * Copyright (c) 2002-2025 Gargoyle Software Inc.
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.host.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.htmlunit.BrowserVersionFeatures.XHR_ALL_RESPONSE_HEADERS_SEPARATE_BY_LF;
import static org.htmlunit.BrowserVersionFeatures.XHR_HANDLE_SYNC_NETWORK_ERRORS;
import static org.htmlunit.BrowserVersionFeatures.XHR_LOAD_ALWAYS_AFTER_DONE;
import static org.htmlunit.BrowserVersionFeatures.XHR_RESPONSE_TEXT_EMPTY_UNSENT;
import static org.htmlunit.BrowserVersionFeatures.XHR_SEND_NETWORK_ERROR_IF_ABORTED;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlunit.AjaxController;
import org.htmlunit.BrowserVersion;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpHeader;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebRequest.HttpHint;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.ContextAction;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.corejs.javascript.json.JsonParser;
import org.htmlunit.corejs.javascript.json.JsonParser.ParseException;
import org.htmlunit.corejs.javascript.typedarrays.NativeArrayBuffer;
import org.htmlunit.corejs.javascript.typedarrays.NativeArrayBufferView;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.httpclient.HtmlUnitUsernamePasswordCredentials;
import org.htmlunit.javascript.HtmlUnitContextFactory;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import org.htmlunit.javascript.background.JavaScriptJob;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;
import org.htmlunit.javascript.host.URLSearchParams;
import org.htmlunit.javascript.host.Window;
import org.htmlunit.javascript.host.dom.DOMException;
import org.htmlunit.javascript.host.dom.DOMParser;
import org.htmlunit.javascript.host.dom.Document;
import org.htmlunit.javascript.host.event.Event;
import org.htmlunit.javascript.host.event.ProgressEvent;
import org.htmlunit.javascript.host.file.Blob;
import org.htmlunit.javascript.host.html.HTMLDocument;
import org.htmlunit.util.EncodingSniffer;
import org.htmlunit.util.MimeType;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.util.WebResponseWrapper;
import org.htmlunit.util.XUserDefinedCharset;
import org.htmlunit.xml.XmlPage;

/**
 * A JavaScript object for an {@code XMLHttpRequest}.
 *
 * @author Daniel Gredler
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Stuart Begg
 * @author Ronald Brill
 * @author Sebastian Cato
 * @author Frank Danek
 * @author Jake Cobb
 * @author Thorsten Wendelmuth
 * @author Lai Quang Duong
 * @author Sven Strickroth
 *
 * @see <a href="http://www.w3.org/TR/XMLHttpRequest/">W3C XMLHttpRequest</a>
 * @see <a href="http://developer.apple.com/internet/webcontent/xmlhttpreq.html">Safari documentation</a>
 */
@JsxClass
public class XMLHttpRequest extends XMLHttpRequestEventTarget {

    private static final Log LOG = LogFactory.getLog(XMLHttpRequest.class);

    /** The object has been created, but not initialized (the open() method has not been called). */
    @JsxConstant
    public static final int UNSENT = 0;

    /** The object has been created, but the send() method has not been called. */
    @JsxConstant
    public static final int OPENED = 1;

    /** The send() method has been called, but the status and headers are not yet available. */
    @JsxConstant
    public static final int HEADERS_RECEIVED = 2;

    /** Some data has been received. */
    @JsxConstant
    public static final int LOADING = 3;

    /** All the data has been received; the complete data is available in responseBody and responseText. */
    @JsxConstant
    public static final int DONE = 4;

    private static final String RESPONSE_TYPE_DEFAULT = "";
    private static final String RESPONSE_TYPE_ARRAYBUFFER = "arraybuffer";
    private static final String RESPONSE_TYPE_BLOB = "blob";
    private static final String RESPONSE_TYPE_DOCUMENT = "document";
    private static final String RESPONSE_TYPE_JSON = "json";
    private static final String RESPONSE_TYPE_TEXT = "text";

    private static final String ALLOW_ORIGIN_ALL = "*";

    private static final HashSet<String> PROHIBITED_HEADERS_ = new HashSet<>(Arrays.asList(
        "accept-charset", HttpHeader.ACCEPT_ENCODING_LC,
        HttpHeader.CONNECTION_LC, HttpHeader.CONTENT_LENGTH_LC, HttpHeader.COOKIE_LC, "cookie2",
        "content-transfer-encoding", "date", "expect",
        HttpHeader.HOST_LC, "keep-alive", HttpHeader.REFERER_LC, "te", "trailer", "transfer-encoding",
        "upgrade", HttpHeader.USER_AGENT_LC, "via"));

    private int state_;
    private WebRequest webRequest_;
    private boolean async_;
    private int jobID_;
    private WebResponse webResponse_;
    private String overriddenMimeType_;
    private boolean withCredentials_;
    private boolean isSameOrigin_;
    private int timeout_;
    private boolean aborted_;
    private String responseType_;

    private Document responseXML_;

    /**
     * Creates a new instance.
     */
    public XMLHttpRequest() {
        state_ = UNSENT;
        responseType_ = RESPONSE_TYPE_DEFAULT;
    }

    /**
     * JavaScript constructor.
     */
    @Override
    @JsxConstructor
    public void jsConstructor() {
        // don't call super here
    }

    /**
     * Sets the state as specified and invokes the state change handler if one has been set.
     * @param state the new state
     */
    private void setState(final int state) {
        if (state == UNSENT
                || state == OPENED
                || state == HEADERS_RECEIVED
                || state == LOADING
                || state == DONE) {
            state_ = state;
            if (LOG.isDebugEnabled()) {
                LOG.debug("State changed to : " + state);
            }
            return;
        }

        LOG.error("Received an unknown state " + state
                        + ", the state is not implemented, please check setState() implementation.");
    }

    private void fireJavascriptEvent(final String eventName) {
        if (aborted_) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Firing javascript XHR event: " + eventName + " for an already aborted request - ignored.");
            }

            return;
        }
        fireJavascriptEventIgnoreAbort(eventName);
    }

    private void fireJavascriptEventIgnoreAbort(final String eventName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Firing javascript XHR event: " + eventName);
        }

        final boolean isReadyStateChange = Event.TYPE_READY_STATE_CHANGE.equalsIgnoreCase(eventName);
        final Event event;
        if (isReadyStateChange) {
            event = new Event(this, Event.TYPE_READY_STATE_CHANGE);
        }
        else {
            final ProgressEvent progressEvent = new ProgressEvent(this, eventName);

            if (webResponse_ != null) {
                final long contentLength = webResponse_.getContentLength();
                progressEvent.setLoaded(contentLength);
            }
            event = progressEvent;
        }

        executeEventLocally(event);
    }

    /**
     * Returns the current state of the HTTP request. The possible values are:
     * <ul>
     *   <li>0 = unsent</li>
     *   <li>1 = opened</li>
     *   <li>2 = headers_received</li>
     *   <li>3 = loading</li>
     *   <li>4 = done</li>
     * </ul>
     * @return the current state of the HTTP request
     */
    @JsxGetter
    public int getReadyState() {
        return state_;
    }

    /**
     * @return the {@code responseType} property
     */
    @JsxGetter
    public String getResponseType() {
        return responseType_;
    }

    /**
     * Sets the {@code responseType} property.
     * @param responseType the {@code responseType} property.
     */
    @JsxSetter
    public void setResponseType(final String responseType) {
        if (state_ == LOADING || state_ == DONE) {
            throw JavaScriptEngine.reportRuntimeError("InvalidStateError");
        }

        if (RESPONSE_TYPE_DEFAULT.equals(responseType)
                || RESPONSE_TYPE_ARRAYBUFFER.equals(responseType)
                || RESPONSE_TYPE_BLOB.equals(responseType)
                || RESPONSE_TYPE_DOCUMENT.equals(responseType)
                || RESPONSE_TYPE_JSON.equals(responseType)
                || RESPONSE_TYPE_TEXT.equals(responseType)) {

            if (state_ == OPENED && !async_) {
                throw JavaScriptEngine.asJavaScriptException(
                        getWindow(),
                        "synchronous XMLHttpRequests do not support responseType",
                        DOMException.INVALID_ACCESS_ERR);
            }

            responseType_ = responseType;
        }
    }

    /**
     * @return returns the response's body content as an ArrayBuffer, Blob, Document, JavaScript Object,
     * or DOMString, depending on the value of the request's responseType property.
     */
    @JsxGetter
    public Object getResponse() {
        if (RESPONSE_TYPE_DEFAULT.equals(responseType_) || RESPONSE_TYPE_TEXT.equals(responseType_)) {
            if (webResponse_ != null) {
                final Charset encoding = webResponse_.getContentCharset();
                final String content = webResponse_.getContentAsString(encoding);
                if (content == null) {
                    return "";
                }
                return content;
            }
        }

        if (state_ != DONE) {
            return null;
        }

        if (webResponse_ instanceof NetworkErrorWebResponse) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("XMLHttpRequest.responseXML returns of a network error ("
                        + ((NetworkErrorWebResponse) webResponse_).getError() + ")");
            }
            return null;
        }

        if (RESPONSE_TYPE_ARRAYBUFFER.equals(responseType_)) {
            long contentLength = webResponse_.getContentLength();
            NativeArrayBuffer nativeArrayBuffer = new NativeArrayBuffer(contentLength);

            try {
                final int bufferLength = Math.min(1024, (int) contentLength);
                final byte[] buffer = new byte[bufferLength];
                int offset = 0;
                try (InputStream inputStream = webResponse_.getContentAsStream()) {
                    int readLen;
                    while ((readLen = inputStream.read(buffer, 0, bufferLength)) != -1) {
                        final long newLength = offset + readLen;
                        // gzip content and the unzipped content is larger
                        if (newLength > contentLength) {
                            final NativeArrayBuffer expanded = new NativeArrayBuffer(newLength);
                            System.arraycopy(nativeArrayBuffer.getBuffer(), 0,
                                    expanded.getBuffer(), 0, (int) contentLength);
                            contentLength = newLength;
                            nativeArrayBuffer = expanded;
                        }
                        System.arraycopy(buffer, 0, nativeArrayBuffer.getBuffer(), offset, readLen);
                        offset = (int) newLength;
                    }
                }

                // for small responses the gzipped content might be larger than the original
                if (offset < contentLength) {
                    final NativeArrayBuffer shrinked = new NativeArrayBuffer(offset);
                    System.arraycopy(nativeArrayBuffer.getBuffer(), 0, shrinked.getBuffer(), 0, offset);
                    nativeArrayBuffer = shrinked;
                }

                nativeArrayBuffer.setParentScope(getParentScope());
                nativeArrayBuffer.setPrototype(
                        ScriptableObject.getClassPrototype(getWindow(), nativeArrayBuffer.getClassName()));

                return nativeArrayBuffer;
            }
            catch (final IOException e) {
                webResponse_ = new NetworkErrorWebResponse(webRequest_, e);
                return null;
            }
        }
        else if (RESPONSE_TYPE_BLOB.equals(responseType_)) {
            try {
                if (webResponse_ != null) {
                    try (InputStream inputStream = webResponse_.getContentAsStream()) {
                        final Blob blob = new Blob(IOUtils.toByteArray(inputStream), webResponse_.getContentType());
                        blob.setParentScope(getParentScope());
                        blob.setPrototype(ScriptableObject.getClassPrototype(getWindow(), blob.getClassName()));

                        return blob;
                    }
                }
            }
            catch (final IOException e) {
                webResponse_ = new NetworkErrorWebResponse(webRequest_, e);
                return null;
            }
        }
        else if (RESPONSE_TYPE_DOCUMENT.equals(responseType_)) {
            if (responseXML_ != null) {
                return responseXML_;
            }

            if (webResponse_ != null) {
                String contentType = webResponse_.getContentType();
                if (StringUtils.isEmpty(contentType)) {
                    contentType = MimeType.TEXT_XML;
                }
                return buildResponseXML(contentType);
            }
        }
        else if (RESPONSE_TYPE_JSON.equals(responseType_)) {
            if (webResponse_ != null) {
                final Charset encoding = webResponse_.getContentCharset();
                final String content = webResponse_.getContentAsString(encoding);
                if (content == null) {
                    return null;
                }

                try {
                    return new JsonParser(Context.getCurrentContext(), this).parseValue(content);
                }
                catch (final ParseException e) {
                    webResponse_ = new NetworkErrorWebResponse(webRequest_, new IOException(e));
                    return null;
                }
            }
        }

        return "";
    }

    private Document buildResponseXML(final String contentType) {
        try {
            if (MimeType.TEXT_XML.equals(contentType)
                    || MimeType.APPLICATION_XML.equals(contentType)
                    || MimeType.APPLICATION_XHTML.equals(contentType)
                    || "image/svg+xml".equals(contentType)) {
                final XMLDocument document = new XMLDocument();
                document.setParentScope(getParentScope());
                document.setPrototype(getPrototype(XMLDocument.class));
                final XmlPage page = new XmlPage(webResponse_, getWindow().getWebWindow(), false);
                if (!page.hasChildNodes()) {
                    return null;
                }
                document.setDomNode(page);
                responseXML_ = document;
                return responseXML_;
            }

            if (MimeType.TEXT_HTML.equals(contentType)) {
                responseXML_ = DOMParser.parseHtmlDocument(this, webResponse_, getWindow().getWebWindow());
                return responseXML_;
            }
            return null;
        }
        catch (final IOException e) {
            webResponse_ = new NetworkErrorWebResponse(webRequest_, e);
            return null;
        }
    }

    /**
     * Returns a string version of the data retrieved from the server.
     * @return a string version of the data retrieved from the server
     */
    @JsxGetter
    public String getResponseText() {
        if ((state_ == UNSENT || state_ == OPENED) && getBrowserVersion().hasFeature(XHR_RESPONSE_TEXT_EMPTY_UNSENT)) {
            return "";
        }

        if (!RESPONSE_TYPE_DEFAULT.equals(responseType_) && !RESPONSE_TYPE_TEXT.equals(responseType_)) {
            throw JavaScriptEngine.asJavaScriptException(
                    getWindow(),
                    "InvalidStateError: Failed to read the 'responseText' property from 'XMLHttpRequest': "
                    + "The value is only accessible if the object's 'responseType' is '' or 'text' "
                            + "(was '" + getResponseType() + "').",
                    DOMException.INVALID_STATE_ERR);
        }

        if (state_ == UNSENT || state_ == OPENED) {
            return "";
        }

        if (webResponse_ instanceof NetworkErrorWebResponse) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("XMLHttpRequest.responseXML returns of a network error ("
                        + ((NetworkErrorWebResponse) webResponse_).getError() + ")");
            }

            final NetworkErrorWebResponse resp = (NetworkErrorWebResponse) webResponse_;
            if (resp.getError() instanceof NoPermittedHeaderException) {
                return "";
            }
            return null;
        }

        if (webResponse_ != null) {
            final Charset encoding = webResponse_.getContentCharset();
            final String content = webResponse_.getContentAsString(encoding);
            if (content == null) {
                return "";
            }
            return content;
        }

        LOG.debug("XMLHttpRequest.responseText was retrieved before the response was available.");
        return "";
    }

    /**
     * Returns a DOM-compatible document object version of the data retrieved from the server.
     * @return a DOM-compatible document object version of the data retrieved from the server
     */
    @JsxGetter
    public Object getResponseXML() {
        if (responseXML_ != null) {
            return responseXML_;
        }

        if (webResponse_ == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("XMLHttpRequest.responseXML returns null because there "
                        + "in no web resonse so far (has send() been called?)");
            }
            return null;
        }

        if (webResponse_ instanceof NetworkErrorWebResponse) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("XMLHttpRequest.responseXML returns of a network error ("
                        + ((NetworkErrorWebResponse) webResponse_).getError() + ")");
            }
            return null;
        }

        String contentType = webResponse_.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            contentType = MimeType.TEXT_XML;
        }

        if (MimeType.TEXT_HTML.equalsIgnoreCase(contentType)) {
            if (!async_ || !RESPONSE_TYPE_DOCUMENT.equals(responseType_)) {
                return null;
            }
        }

        return buildResponseXML(contentType);
    }

    /**
     * Returns the numeric status returned by the server, such as 404 for "Not Found"
     * or 200 for "OK".
     * @return the numeric status returned by the server
     */
    @JsxGetter
    public int getStatus() {
        if (state_ == UNSENT || state_ == OPENED) {
            return 0;
        }
        if (webResponse_ != null) {
            return webResponse_.getStatusCode();
        }

        if (LOG.isErrorEnabled()) {
            LOG.error("XMLHttpRequest.status was retrieved without a response available (readyState: "
                + state_ + ").");
        }
        return 0;
    }

    /**
     * Returns the string message accompanying the status code, such as "Not Found" or "OK".
     * @return the string message accompanying the status code
     */
    @JsxGetter
    public String getStatusText() {
        if (state_ == UNSENT || state_ == OPENED) {
            return "";
        }
        if (webResponse_ != null) {
            return webResponse_.getStatusMessage();
        }

        if (LOG.isErrorEnabled()) {
            LOG.error("XMLHttpRequest.statusText was retrieved without a response available (readyState: "
                + state_ + ").");
        }
        return null;
    }

    /**
     * Cancels the current HTTP request.
     */
    @JsxFunction
    public void abort() {
        getWindow().getWebWindow().getJobManager().stopJob(jobID_);

        if (state_ == OPENED
                || state_ == HEADERS_RECEIVED
                || state_ == LOADING) {
            setState(DONE);
            webResponse_ = new NetworkErrorWebResponse(webRequest_, null);
            fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
            fireJavascriptEvent(Event.TYPE_ABORT);
            fireJavascriptEvent(Event.TYPE_LOAD_END);
        }

        // JavaScriptEngine.constructError("NetworkError",
        //         "Failed to execute 'send' on 'XMLHttpRequest': Failed to load '" + webRequest_.getUrl() + "'");

        setState(UNSENT);
        webResponse_ = new NetworkErrorWebResponse(webRequest_, null);
        aborted_ = true;
    }

    /**
     * Returns the labels and values of all the HTTP headers.
     * @return the labels and values of all the HTTP headers
     */
    @JsxFunction
    public String getAllResponseHeaders() {
        if (state_ == UNSENT || state_ == OPENED) {
            return "";
        }
        if (webResponse_ != null) {
            final StringBuilder builder = new StringBuilder();
            for (final NameValuePair header : webResponse_.getResponseHeaders()) {
                builder.append(header.getName()).append(": ").append(header.getValue());

                if (!getBrowserVersion().hasFeature(XHR_ALL_RESPONSE_HEADERS_SEPARATE_BY_LF)) {
                    builder.append('\r');
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        if (LOG.isErrorEnabled()) {
            LOG.error("XMLHttpRequest.getAllResponseHeaders() was called without a response available (readyState: "
                + state_ + ").");
        }
        return null;
    }

    /**
     * Retrieves the value of an HTTP header from the response body.
     * @param headerName the (case-insensitive) name of the header to retrieve
     * @return the value of the specified HTTP header
     */
    @JsxFunction
    public String getResponseHeader(final String headerName) {
        if (state_ == UNSENT || state_ == OPENED) {
            return null;
        }
        if (webResponse_ != null) {
            return webResponse_.getResponseHeaderValue(headerName);
        }

        if (LOG.isErrorEnabled()) {
            LOG.error("XMLHttpRequest.getAllResponseHeaders(..) was called without a response available (readyState: "
                + state_ + ").");
        }
        return null;
    }

    /**
     * Assigns the destination URL, method and other optional attributes of a pending request.
     * @param method the method to use to send the request to the server (GET, POST, etc)
     * @param urlParam the URL to send the request to
     * @param asyncParam Whether or not to send the request to the server asynchronously, defaults to {@code true}
     * @param user If authentication is needed for the specified URL, the username to use to authenticate
     * @param password If authentication is needed for the specified URL, the password to use to authenticate
     */
    @JsxFunction
    public void open(final String method, final Object urlParam, final Object asyncParam,
        final Object user, final Object password) {

        // async defaults to true if not specified
        boolean async = true;
        if (!JavaScriptEngine.isUndefined(asyncParam)) {
            async = JavaScriptEngine.toBoolean(asyncParam);
        }

        final String url = JavaScriptEngine.toString(urlParam);

        // (URL + Method + User + Password) become a WebRequest instance.
        final HtmlPage containingPage = (HtmlPage) getWindow().getWebWindow().getEnclosedPage();

        try {
            final URL pageUrl = containingPage.getUrl();
            final URL fullUrl = containingPage.getFullyQualifiedUrl(url);
            final WebRequest request = new WebRequest(fullUrl, getBrowserVersion().getXmlHttpRequestAcceptHeader(),
                                                                getBrowserVersion().getAcceptEncodingHeader());
            request.setCharset(UTF_8);
            // https://xhr.spec.whatwg.org/#response-body
            request.setDefaultResponseContentCharset(UTF_8);
            request.setRefererHeader(pageUrl);

            try {
                request.setHttpMethod(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)));
            }
            catch (final IllegalArgumentException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Incorrect HTTP Method '" + method + "'");
                }
                return;
            }

            isSameOrigin_ = isSameOrigin(pageUrl, fullUrl);
            final boolean alwaysAddOrigin = HttpMethod.GET != request.getHttpMethod()
                                            && HttpMethod.PATCH != request.getHttpMethod()
                                            && HttpMethod.HEAD != request.getHttpMethod();
            if (alwaysAddOrigin || !isSameOrigin_) {
                final StringBuilder origin = new StringBuilder().append(pageUrl.getProtocol()).append("://")
                        .append(pageUrl.getHost());
                if (pageUrl.getPort() != -1) {
                    origin.append(':').append(pageUrl.getPort());
                }
                request.setAdditionalHeader(HttpHeader.ORIGIN, origin.toString());
            }

            // password is ignored if no user defined
            if (user != null && !JavaScriptEngine.isUndefined(user)) {
                final String userCred = user.toString();

                String passwordCred = "";
                if (password != null && !JavaScriptEngine.isUndefined(password)) {
                    passwordCred = password.toString();
                }

                request.setCredentials(new HtmlUnitUsernamePasswordCredentials(userCred, passwordCred.toCharArray()));
            }
            webRequest_ = request;
        }
        catch (final MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to initialize XMLHttpRequest using malformed URL '" + url + "'.");
            }
            return;
        }

        // Async stays a boolean.
        async_ = async;

        // Change the state!
        setState(OPENED);
        fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
    }

    private static boolean isSameOrigin(final URL originUrl, final URL newUrl) {
        if (!originUrl.getHost().equals(newUrl.getHost())) {
            return false;
        }

        int originPort = originUrl.getPort();
        if (originPort == -1) {
            originPort = originUrl.getDefaultPort();
        }
        int newPort = newUrl.getPort();
        if (newPort == -1) {
            newPort = newUrl.getDefaultPort();
        }
        return originPort == newPort;
    }

    /**
     * Sends the specified content to the server in an HTTP request and receives the response.
     * @param content the body of the message being sent with the request
     */
    @JsxFunction
    public void send(final Object content) {
        responseXML_ = null;

        if (webRequest_ == null) {
            return;
        }
        if (!async_ && timeout_ > 0) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(
                    new RuntimeException("Synchronous requests must not set a timeout."));
        }

        prepareRequestContent(content);
        if (timeout_ > 0) {
            webRequest_.setTimeout(timeout_);
        }

        final Window w = getWindow();
        final WebWindow ww = w.getWebWindow();
        final WebClient client = ww.getWebClient();
        final AjaxController ajaxController = client.getAjaxController();
        final HtmlPage page = (HtmlPage) ww.getEnclosedPage();
        final boolean synchron = ajaxController.processSynchron(page, webRequest_, async_);
        if (synchron) {
            doSend();
        }
        else {
            // Create and start a thread in which to execute the request.
            final HtmlUnitContextFactory cf = client.getJavaScriptEngine().getContextFactory();
            final ContextAction<Object> action = new ContextAction<Object>() {
                @Override
                public Object run(final Context cx) {
                    doSend();
                    return null;
                }

                @Override
                public String toString() {
                    return "XMLHttpRequest " + webRequest_.getHttpMethod() + " '" + webRequest_.getUrl() + "'";
                }
            };
            final JavaScriptJob job = BackgroundJavaScriptFactory.theFactory().
                    createJavascriptXMLHttpRequestJob(cf, action);
            LOG.debug("Starting XMLHttpRequest thread for asynchronous request");
            jobID_ = ww.getJobManager().addJob(job, page);

            fireJavascriptEvent(Event.TYPE_LOAD_START);
        }
    }

    /**
     * Prepares the WebRequest that will be sent.
     * @param content the content to send
     */
    private void prepareRequestContent(final Object content) {
        if (content != null
            && (HttpMethod.POST == webRequest_.getHttpMethod()
                    || HttpMethod.PUT == webRequest_.getHttpMethod()
                    || HttpMethod.PATCH == webRequest_.getHttpMethod()
                    || HttpMethod.DELETE == webRequest_.getHttpMethod()
                    || HttpMethod.OPTIONS == webRequest_.getHttpMethod())
            && !JavaScriptEngine.isUndefined(content)) {

            final boolean setEncodingType = webRequest_.getAdditionalHeader(HttpHeader.CONTENT_TYPE) == null;

            if (content instanceof HTMLDocument) {
                // final String body = ((HTMLDocument) content).getDomNodeOrDie().asXml();
                final String body = new XMLSerializer().serializeToString((HTMLDocument) content);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting request body to: " + body);
                }
                webRequest_.setRequestBody(body);
                if (setEncodingType) {
                    webRequest_.setAdditionalHeader(HttpHeader.CONTENT_TYPE, "text/html;charset=UTF-8");
                }
            }
            else if (content instanceof XMLDocument) {
                // this output differs from real browsers but it seems to be a good starting point
                try (StringWriter writer = new StringWriter()) {
                    final XMLDocument xmlDocument = (XMLDocument) content;

                    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.setOutputProperty(OutputKeys.INDENT, "no");
                    transformer.transform(
                            new DOMSource(xmlDocument.getDomNodeOrDie().getFirstChild()), new StreamResult(writer));

                    final String body = writer.toString();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Setting request body to: " + body);
                    }
                    webRequest_.setRequestBody(body);
                    if (setEncodingType) {
                        webRequest_.setAdditionalHeader(HttpHeader.CONTENT_TYPE,
                                        MimeType.APPLICATION_XML + ";charset=UTF-8");
                    }
                }
                catch (final Exception e) {
                    throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
                }
            }
            else if (content instanceof FormData) {
                ((FormData) content).fillRequest(webRequest_);
            }
            else if (content instanceof NativeArrayBufferView) {
                final NativeArrayBufferView view = (NativeArrayBufferView) content;
                webRequest_.setRequestBody(new String(view.getBuffer().getBuffer(), UTF_8));
                if (setEncodingType) {
                    webRequest_.setEncodingType(null);
                }
            }
            else if (content instanceof URLSearchParams) {
                ((URLSearchParams) content).fillRequest(webRequest_);
                webRequest_.addHint(HttpHint.IncludeCharsetInContentTypeHeader);
            }
            else if (content instanceof Blob) {
                ((Blob) content).fillRequest(webRequest_);
            }
            else {
                final String body = JavaScriptEngine.toString(content);
                if (!body.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Setting request body to: " + body);
                    }
                    webRequest_.setRequestBody(body);
                    webRequest_.setCharset(UTF_8);
                    if (setEncodingType) {
                        webRequest_.setEncodingType(FormEncodingType.TEXT_PLAIN);
                    }
                }
            }
        }
        //TODO HA #1414 start
        webRequest_.setXHR();
        // HA end
    }

    /**
     * The real send job.
     */
    void doSend() {
        final WebClient wc = getWindow().getWebWindow().getWebClient();

        // accessing to local resource is forbidden for security reason
        if (!wc.getOptions().isFileProtocolForXMLHttpRequestsAllowed()
                && "file".equals(webRequest_.getUrl().getProtocol())) {

            if (async_) {
                setState(DONE);
                fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
                fireJavascriptEvent(Event.TYPE_ERROR);
                fireJavascriptEvent(Event.TYPE_LOAD_END);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Not allowed to load local resource: " + webRequest_.getUrl());
            }
            throw JavaScriptEngine.asJavaScriptException(
                    getWindow(),
                    "Not allowed to load local resource: " + webRequest_.getUrl(),
                    DOMException.NETWORK_ERR);
        }

        final BrowserVersion browserVersion = getBrowserVersion();
        try {
            if (!isSameOrigin_ && isPreflight()) {
                final WebRequest preflightRequest = new WebRequest(webRequest_.getUrl(), HttpMethod.OPTIONS);

                // preflight request shouldn't have cookies
                preflightRequest.addHint(HttpHint.BlockCookies);

                // header origin
                final String originHeaderValue = webRequest_.getAdditionalHeaders().get(HttpHeader.ORIGIN);
                preflightRequest.setAdditionalHeader(HttpHeader.ORIGIN, originHeaderValue);

                // header request-method
                preflightRequest.setAdditionalHeader(
                        HttpHeader.ACCESS_CONTROL_REQUEST_METHOD,
                        webRequest_.getHttpMethod().name());

                // header request-headers
                final StringBuilder builder = new StringBuilder();
                for (final Entry<String, String> header
                        : new TreeMap<>(webRequest_.getAdditionalHeaders()).entrySet()) {
                    final String name = org.htmlunit.util.StringUtils
                                            .toRootLowerCase(header.getKey());
                    if (isPreflightHeader(name, header.getValue())) {
                        if (builder.length() != 0) {
                            builder.append(',');
                        }
                        builder.append(name);
                    }
                }
                preflightRequest.setAdditionalHeader(HttpHeader.ACCESS_CONTROL_REQUEST_HEADERS, builder.toString());
                if (timeout_ > 0) {
                    preflightRequest.setTimeout(timeout_);
                }

                // TODO HA #2932, #1414 start
                preflightRequest.setXHR();
                // TODO HA #2932, #1414 end

                // do the preflight request
                final WebResponse preflightResponse = wc.loadWebResponse(preflightRequest);
                if (!preflightResponse.isSuccessOrUseProxyOrNotModified()
                        || !isPreflightAuthorized(preflightResponse)) {
                    setState(DONE);
                    if (async_ || browserVersion.hasFeature(XHR_HANDLE_SYNC_NETWORK_ERRORS)) {
                        fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
                        fireJavascriptEvent(Event.TYPE_ERROR);
                        fireJavascriptEvent(Event.TYPE_LOAD_END);
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No permitted request for URL " + webRequest_.getUrl());
                    }
                    throw JavaScriptEngine.asJavaScriptException(
                            getWindow(),
                            "No permitted \"Access-Control-Allow-Origin\" header.",
                            DOMException.NETWORK_ERR);
                }
            }

            if (!isSameOrigin_) {
                // Cookies should not be sent for cross-origin requests when withCredentials is false
                if (!isWithCredentials()) {
                    webRequest_.addHint(HttpHint.BlockCookies);
                }
            }

            webResponse_ = wc.loadWebResponse(webRequest_);
            LOG.debug("Web response loaded successfully.");

            boolean allowOriginResponse = true;
            if (!isSameOrigin_) {
                String value = webResponse_.getResponseHeaderValue(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN);
                allowOriginResponse = webRequest_.getAdditionalHeaders().get(HttpHeader.ORIGIN).equals(value);
                if (isWithCredentials()) {
                    // second step: check the allow-credentials header for true
                    value = webResponse_.getResponseHeaderValue(HttpHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                    allowOriginResponse = allowOriginResponse && Boolean.parseBoolean(value);
                }
                else {
                    allowOriginResponse = allowOriginResponse || ALLOW_ORIGIN_ALL.equals(value);
                }
            }
            if (allowOriginResponse) {
                if (overriddenMimeType_ != null) {
                    final int index = overriddenMimeType_.toLowerCase(Locale.ROOT).indexOf("charset=");
                    String charsetName = "";
                    if (index != -1) {
                        charsetName = overriddenMimeType_.substring(index + "charset=".length());
                    }

                    final String charsetNameFinal = charsetName;
                    final Charset charset;
                    if (XUserDefinedCharset.NAME.equalsIgnoreCase(charsetName)) {
                        charset = XUserDefinedCharset.INSTANCE;
                    }
                    else {
                        charset = EncodingSniffer.toCharset(charsetName);
                    }
                    webResponse_ = new WebResponseWrapper(webResponse_) {
                        @Override
                        public String getContentType() {
                            return overriddenMimeType_;
                        }

                        @Override
                        public Charset getContentCharset() {
                            if (charsetNameFinal.isEmpty() || charset == null) {
                                return super.getContentCharset();
                            }
                            return charset;
                        }
                    };
                }
            }
            if (!allowOriginResponse) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No permitted \"Access-Control-Allow-Origin\" header for URL " + webRequest_.getUrl());
                }
                throw new NoPermittedHeaderException("No permitted \"Access-Control-Allow-Origin\" header.");
            }

            setState(HEADERS_RECEIVED);
            if (async_) {
                fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);

                setState(LOADING);
                fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
                fireJavascriptEvent(Event.TYPE_PROGRESS);
            }

            setState(DONE);
            fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);

            if (!async_ && aborted_
                    && browserVersion.hasFeature(XHR_SEND_NETWORK_ERROR_IF_ABORTED)) {
                throw JavaScriptEngine.constructError("Error",
                        "Failed to execute 'send' on 'XMLHttpRequest': Failed to load '" + webRequest_.getUrl() + "'");
            }

            if (browserVersion.hasFeature(XHR_LOAD_ALWAYS_AFTER_DONE)) {
                fireJavascriptEventIgnoreAbort(Event.TYPE_LOAD);
                fireJavascriptEventIgnoreAbort(Event.TYPE_LOAD_END);
            }
            else {
                fireJavascriptEvent(Event.TYPE_LOAD);
                fireJavascriptEvent(Event.TYPE_LOAD_END);
            }
        }
        catch (final IOException e) {
            LOG.debug("IOException: returning a network error response.", e);

            webResponse_ = new NetworkErrorWebResponse(webRequest_, e);
            if (async_) {
                setState(DONE);
                fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
                if (e instanceof SocketTimeoutException) {
                    fireJavascriptEvent(Event.TYPE_TIMEOUT);
                }
                else {
                    fireJavascriptEvent(Event.TYPE_ERROR);
                }
                fireJavascriptEvent(Event.TYPE_LOAD_END);
            }
            else {
                setState(DONE);
                if (browserVersion.hasFeature(XHR_HANDLE_SYNC_NETWORK_ERRORS)) {
                    fireJavascriptEvent(Event.TYPE_READY_STATE_CHANGE);
                    if (e instanceof SocketTimeoutException) {
                        fireJavascriptEvent(Event.TYPE_TIMEOUT);
                    }
                    else {
                        fireJavascriptEvent(Event.TYPE_ERROR);
                    }
                    fireJavascriptEvent(Event.TYPE_LOAD_END);
                }

                throw JavaScriptEngine.asJavaScriptException(getWindow(),
                        e.getMessage(), DOMException.NETWORK_ERR);
            }
        }
    }

    private boolean isPreflight() {
        final HttpMethod method = webRequest_.getHttpMethod();
        if (method != HttpMethod.GET && method != HttpMethod.HEAD && method != HttpMethod.POST) {
            return true;
        }
        for (final Entry<String, String> header : webRequest_.getAdditionalHeaders().entrySet()) {
            if (isPreflightHeader(header.getKey().toLowerCase(Locale.ROOT), header.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreflightAuthorized(final WebResponse preflightResponse) {
        final String originHeader = preflightResponse.getResponseHeaderValue(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN);
        if (!ALLOW_ORIGIN_ALL.equals(originHeader)
                && !webRequest_.getAdditionalHeaders().get(HttpHeader.ORIGIN).equals(originHeader)) {
            return false;
        }

        // there is no test case for this because the servlet API has no support
        // for adding the same header twice
        final HashSet<String> accessControlValues = new HashSet<>();
        for (final NameValuePair pair : preflightResponse.getResponseHeaders()) {
            if (HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS.equalsIgnoreCase(pair.getName())) {
                String value = pair.getValue();
                if (value != null) {
                    value = org.htmlunit.util.StringUtils.toRootLowerCase(value);
                    final String[] values = org.htmlunit.util.StringUtils.splitAtComma(value);
                    for (String part : values) {
                        part = part.trim();
                        if (StringUtils.isNotEmpty(part)) {
                            accessControlValues.add(part);
                        }
                    }
                }
            }
        }

        for (final Entry<String, String> header : webRequest_.getAdditionalHeaders().entrySet()) {
            final String key = org.htmlunit.util.StringUtils.toRootLowerCase(header.getKey());
            if (isPreflightHeader(key, header.getValue())
                    && !accessControlValues.contains(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param name header name (MUST be lower-case for performance reasons)
     * @param value header value
     */
    private static boolean isPreflightHeader(final String name, final String value) {
        if (HttpHeader.CONTENT_TYPE_LC.equals(name)) {
            final String lcValue = value.toLowerCase(Locale.ROOT);
            return !lcValue.startsWith(FormEncodingType.URL_ENCODED.getName())
                    && !lcValue.startsWith(FormEncodingType.MULTIPART.getName())
                    && !lcValue.startsWith(FormEncodingType.TEXT_PLAIN.getName());
        }
        if (HttpHeader.ACCEPT_LC.equals(name)
                || HttpHeader.ACCEPT_LANGUAGE_LC.equals(name)
                || HttpHeader.CONTENT_LANGUAGE_LC.equals(name)
                || HttpHeader.REFERER_LC.equals(name)
                || "accept-encoding".equals(name)
                || HttpHeader.ORIGIN_LC.equals(name)) {
            return false;
        }
        return true;
    }

    /**
     * Sets the specified header to the specified value. The <code>open</code> method must be
     * called before this method, or an error will occur.
     * @param name the name of the header being set
     * @param value the value of the header being set
     */
    @JsxFunction
    public void setRequestHeader(final String name, final String value) {
        if (!isAuthorizedHeader(name)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Ignoring XMLHttpRequest.setRequestHeader for " + name
                    + ": it is a restricted header");
            }
            return;
        }

        if (webRequest_ != null) {
            webRequest_.setAdditionalHeader(name, value);
        }
        else {
            throw JavaScriptEngine.asJavaScriptException(
                    getWindow(),
                    "The open() method must be called before setRequestHeader().",
                    DOMException.INVALID_STATE_ERR);
        }
    }

    /**
     * Not all request headers can be set from JavaScript.
     * @see <a href="http://www.w3.org/TR/XMLHttpRequest/#the-setrequestheader-method">W3C doc</a>
     * @param name the header name
     * @return {@code true} if the header can be set from JavaScript
     */
    static boolean isAuthorizedHeader(final String name) {
        final String nameLowerCase = org.htmlunit.util.StringUtils.toRootLowerCase(name);
        if (PROHIBITED_HEADERS_.contains(nameLowerCase)) {
            return false;
        }
        if (nameLowerCase.startsWith("proxy-") || nameLowerCase.startsWith("sec-")) {
            return false;
        }
        return true;
    }

    /**
     * Override the mime type returned by the server (if any). This may be used, for example, to force a stream
     * to be treated and parsed as text/xml, even if the server does not report it as such.
     * This must be done before the send method is invoked.
     * @param mimeType the type used to override that returned by the server (if any)
     * @see <a href="http://xulplanet.com/references/objref/XMLHttpRequest.html#method_overrideMimeType">XUL Planet</a>
     */
    @JsxFunction
    public void overrideMimeType(final String mimeType) {
        if (state_ != UNSENT && state_ != OPENED) {
            throw JavaScriptEngine.asJavaScriptException(
                    getWindow(),
                    "Property 'overrideMimeType' not writable after sent.",
                    DOMException.INVALID_STATE_ERR);
        }
        overriddenMimeType_ = mimeType;
    }

    /**
     * Returns the {@code withCredentials} property.
     * @return the {@code withCredentials} property
     */
    @JsxGetter
    public boolean isWithCredentials() {
        return withCredentials_;
    }

    /**
     * Sets the {@code withCredentials} property.
     * @param withCredentials the {@code withCredentials} property.
     */
    @JsxSetter
    public void setWithCredentials(final boolean withCredentials) {
        withCredentials_ = withCredentials;
    }

    /**
     * Returns the {@code upload} property.
     * @return the {@code upload} property
     */
    @JsxGetter
    public XMLHttpRequestUpload getUpload() {
        final XMLHttpRequestUpload upload = new XMLHttpRequestUpload();
        upload.setParentScope(getParentScope());
        upload.setPrototype(getPrototype(upload.getClass()));
        return upload;
    }

    /**
     * {@inheritDoc}
     */
    @JsxGetter
    @Override
    public Function getOnreadystatechange() {
        return super.getOnreadystatechange();
    }

    /**
     * {@inheritDoc}
     */
    @JsxSetter
    @Override
    public void setOnreadystatechange(final Function readyStateChangeHandler) {
        super.setOnreadystatechange(readyStateChangeHandler);
    }

    @JsxGetter
    public int getTimeout() {
        return timeout_;
    }

    @JsxSetter
    public void setTimeout(final int timeout) {
        timeout_ = timeout;
    }

    private static final class NetworkErrorWebResponse extends WebResponse {
        private final WebRequest request_;
        private final IOException error_;

        NetworkErrorWebResponse(final WebRequest webRequest, final IOException error) {
            super(null, null, 0);
            request_ = webRequest;
            error_ = error;
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getStatusMessage() {
            return "";
        }

        @Override
        public String getContentType() {
            return "";
        }

        @Override
        public String getContentAsString() {
            return "";
        }

        @Override
        public InputStream getContentAsStream() {
            return null;
        }

        @Override
        public List<NameValuePair> getResponseHeaders() {
            return Collections.emptyList();
        }

        @Override
        public String getResponseHeaderValue(final String headerName) {
            return "";
        }

        @Override
        public long getLoadTime() {
            return 0;
        }

        @Override
        public Charset getContentCharset() {
            return null;
        }

        @Override
        public Charset getContentCharsetOrNull() {
            return null;
        }

        @Override
        public WebRequest getWebRequest() {
            return request_;
        }

        /**
         * @return the error
         */
        public IOException getError() {
            return error_;
        }
    }

    private static final class NoPermittedHeaderException extends IOException {
        NoPermittedHeaderException(final String msg) {
            super(msg);
        }
    }
}
