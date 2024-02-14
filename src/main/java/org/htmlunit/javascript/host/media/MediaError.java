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
package org.htmlunit.javascript.host.media;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;
import org.htmlunit.javascript.configuration.JsxConstructor;

/**
 * A JavaScript object for {@code MediaError}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class MediaError extends HtmlUnitScriptable {

    /** Aborted Error. */
    @JsxConstant
    public static final int MEDIA_ERR_ABORTED = 1;

    /** Network Error. */
    @JsxConstant
    public static final int MEDIA_ERR_NETWORK = 2;

    /** Decode Error. */
    @JsxConstant
    public static final int MEDIA_ERR_DECODE = 3;

    /** Source Not Supported Error. */
    @JsxConstant
    public static final int MEDIA_ERR_SRC_NOT_SUPPORTED = 4;

    /** Source Not Supported Error. */
    @JsxConstant(IE)
    public static final int MS_MEDIA_ERR_ENCRYPTED = 5;

    /**
     * Creates an instance.
     */
    public MediaError() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }
}
