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

import static org.htmlunit.BrowserVersionFeatures.JS_BLOB_EVENT_REQUIRES_DATA;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.host.file.Blob;

/**
 * A JavaScript object for {@code BlobEvent}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass({CHROME, EDGE, FF, FF, FF_ESR})
public class BlobEvent extends Event {

    /** The data passed when initializing the event. */
    private Blob data_;

    /**
     * Creates an instance.
     */
    public BlobEvent() {
    }

    /**
     * JavaScript constructor.
     *
     * @param type the event type
     * @param details the event details (optional)
     */
    @Override
    @JsxConstructor
    public void jsConstructor(final String type, final ScriptableObject details) {
        super.jsConstructor(JavaScriptEngine.toString(type), details);

        if (details != null && !JavaScriptEngine.isUndefined(details)) {
            final Object dataObj = details.get("data", details);
            if (NOT_FOUND == dataObj) {
                if (getBrowserVersion().hasFeature(JS_BLOB_EVENT_REQUIRES_DATA)) {
                    throw JavaScriptEngine.typeError("BlobEvent data is required.");
                }
            }
            else {
                if (!(dataObj instanceof Blob)) {
                    throw JavaScriptEngine.typeError("BlobEvent data has to be a Blob.");
                }
                data_ = (Blob) dataObj;
            }
        }
        else {
            if (getBrowserVersion().hasFeature(JS_BLOB_EVENT_REQUIRES_DATA)) {
                throw JavaScriptEngine.typeError("BlobEvent data is required.");
            }
        }
    }

    /**
     * @return the Blob associated with the event
     */
    @JsxGetter
    public Blob getData() {
        return data_;
    }

    /**
     * Sets the associated with the event.
     *
     * @param data associated with the event
     */
    protected void setDetail(final Blob data) {
        data_ = data;
    }

}
