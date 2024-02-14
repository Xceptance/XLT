/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
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
package org.htmlunit.javascript.host.event;

import static org.htmlunit.BrowserVersionFeatures.EVENT_BEFORE_UNLOAD_RETURN_VALUE_IS_HTML5_LIKE;
import static org.htmlunit.BrowserVersionFeatures.EVENT_HANDLER_NULL_RETURN_IS_MEANINGFUL;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.BrowserVersion;
import org.htmlunit.corejs.javascript.Undefined;
import org.htmlunit.html.DomNode;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * JavaScript object representing the BeforeUnloadEvent.
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/window.onbeforeunload">Mozilla Developer Network</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/ie/ff974336.aspx">MSDN</a>
 *
 * @author Frank Danek
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Atsushi Nakagawa
 */
@JsxClass
public class BeforeUnloadEvent extends Event {
    private Object returnValue_;

    /**
     * Creates a new event instance.
     */
    public BeforeUnloadEvent() {
        setType("");
        returnValue_ = "";
    }

    /**
     * The JavaScript constructor. It seems it is not possible to do it from JavaScript code.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
        throw JavaScriptEngine.throwAsScriptRuntimeEx(new IllegalArgumentException("Illegal Constructor"));
    }

    /**
     * Creates a new event instance.
     *
     * @param domNode the DOM node that triggered the event
     * @param type the event type
     */
    public BeforeUnloadEvent(final DomNode domNode, final String type) {
        super(domNode, type);

        setBubbles(false);
        setReturnValue(getReturnValueDefault(getBrowserVersion()));
    }

    @Override
    public void initEvent(final String type, final boolean bubbles, final boolean cancelable) {
        super.initEvent(type, bubbles, cancelable);
        setReturnValue(getReturnValueDefault(getBrowserVersion()));
    }

    private static Object getReturnValueDefault(final BrowserVersion browserVersion) {
        if (browserVersion.hasFeature(EVENT_BEFORE_UNLOAD_RETURN_VALUE_IS_HTML5_LIKE)) {
            // Empty string default is specified by HTML5
            // https://www.w3.org/TR/html5/browsers.html#the-beforeunloadevent-interface
            return "";
        }
        return Undefined.instance;
    }

    /**
     * @return {@code true} if returnValue holds the beforeunload message
     */
    public boolean isBeforeUnloadMessageSet() {
        return !getReturnValueDefault(getBrowserVersion()).equals(getReturnValue());
    }

    /**
     * @return the return value associated with the event
     */
    @JsxGetter
    @Override
    public Object getReturnValue() {
        return returnValue_;
    }

    /**
     * Sets the return value associated with the event.
     * @param returnValue the return value associated with the event
     */
    @JsxSetter
    @Override
    public void setReturnValue(final Object returnValue) {
        returnValue_ = returnValue;
    }

    @Override
    void handlePropertyHandlerReturnValue(final Object returnValue) {
        super.handlePropertyHandlerReturnValue(returnValue);

        final BrowserVersion browserVersion = getBrowserVersion();

        // Most browsers ignore null return values of property handlers
        if (returnValue != null || browserVersion.hasFeature(EVENT_HANDLER_NULL_RETURN_IS_MEANINGFUL)) {
            // Chrome/Firefox only accept the return value if returnValue is equal to default
            if (!browserVersion.hasFeature(EVENT_BEFORE_UNLOAD_RETURN_VALUE_IS_HTML5_LIKE)
                    || getReturnValueDefault(browserVersion).equals(getReturnValue())) {
                setReturnValue(returnValue);
            }
        }
    }
}
