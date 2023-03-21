/*
 * Copyright (c) 2002-2022 Gargoyle Software Inc.
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

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.host.event.EventTarget;

/**
 * A JavaScript object for {@code BaseAudioContext}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass({CHROME, EDGE, FF, FF_ESR})
public class BaseAudioContext extends EventTarget {

    /**
     * Creates an instance.
     */
    @JsxConstructor
    public BaseAudioContext() {
    }

    /**
     * @return a new AudioBufferSourceNode, which can be used to
     * play audio data contained within an AudioBuffer object.
     */
    @JsxFunction
    public AudioBufferSourceNode createBufferSource() {
        final AudioBufferSourceNode node = new AudioBufferSourceNode();
        node.setParentScope(getParentScope());
        node.setPrototype(getPrototype(node.getClass()));
        return node;
    }

    /**
     * @return new, empty AudioBuffer object, which can then be
     * populated by data, and played via an AudioBufferSourceNode.
     */
    @JsxFunction
    public AudioBuffer createBuffer() {
        final AudioBuffer node = new AudioBuffer();
        node.setParentScope(getParentScope());
        node.setPrototype(getPrototype(node.getClass()));
        return node;
    }
}
