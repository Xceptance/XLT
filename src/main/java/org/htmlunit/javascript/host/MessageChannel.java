/*
 * Copyright (c) 2002-2025 Gargoyle Software Inc.
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
package org.htmlunit.javascript.host;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code MessageChannel}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class MessageChannel extends HtmlUnitScriptable {

    private MessagePort port1_;
    private MessagePort port2_;

    /**
     * JavaScript constructor.
     */
    @JsxConstructor
    public void jsConstructor() {
        // nothing to do
    }

    /**
     * Returns {@code port1} property.
     * @return {@code port1} property.
     */
    @JsxGetter
    public MessagePort getPort1() {
        if (port1_ == null) {
            port1_ = new MessagePort();
            port1_.setParentScope(getParentScope());
            port1_.setPrototype(getPrototype(port1_.getClass()));
        }
        return port1_;
    }

    /**
     * Returns {@code port2} property.
     * @return {@code port2} property.
     */
    @JsxGetter
    public MessagePort getPort2() {
        if (port2_ == null) {
            port2_ = new MessagePort(getPort1());
            port2_.setParentScope(getParentScope());
            port2_.setPrototype(getPrototype(port2_.getClass()));
        }
        return port2_;
    }
}
