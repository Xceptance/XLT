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
package org.htmlunit.javascript.host.speech;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.host.event.EventTarget;

/**
 * A JavaScript object for {@code webkitSpeechRecognition}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(value = {CHROME, EDGE}, className = "webkitSpeechRecognition")
public class WebkitSpeechRecognition extends EventTarget {

    /**
     * Creates a new instance.
     */
    public WebkitSpeechRecognition() {
    }

    /**
     * JavaScript constructor.
     */
    @Override
    @JsxConstructor(functionName = "SpeechRecognition")
    public void jsConstructor() {
        super.jsConstructor();
    }
}
