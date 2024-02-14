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

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code CustomEvent}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class CustomEvent extends Event {

    /** The data passed when initializing the event. */
    private Object detail_;

    /**
     * Default constructor.
     */
    public CustomEvent() {
    }

    /**
     * JavaScript constructor.
     *
     * @param type the event type
     * @param details the event details (optional)
     */
    @Override
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor(final String type, final ScriptableObject details) {
        super.jsConstructor(JavaScriptEngine.toString(type), details);

        if (details != null && !JavaScriptEngine.isUndefined(details)) {
            final Object detail = details.get("detail", details);
            if (NOT_FOUND != detail) {
                detail_ = detail;
            }
        }
    }

    /**
     * Implementation of the DOM Level 2 Event method for initializing the mouse event.
     *
     * @param type the event type
     * @param bubbles can the event bubble
     * @param cancelable can the event be canceled
     * @param detail the detail to set for the event
     */
    @JsxFunction
    public void initCustomEvent(
            final String type,
            final boolean bubbles,
            final boolean cancelable,
            final Object detail) {
        initEvent(type, bubbles, cancelable);
        detail_ = detail;
    }

    /**
     * Returns any data passed when initializing the event.
     * @return any data passed when initializing the event
     */
    @JsxGetter
    public Object getDetail() {
        return detail_;
    }
}
