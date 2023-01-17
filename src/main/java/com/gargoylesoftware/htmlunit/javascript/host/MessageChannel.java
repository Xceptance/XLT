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
package com.gargoylesoftware.htmlunit.javascript.host;

import com.gargoylesoftware.htmlunit.javascript.HtmlUnitScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code MessageChannel}.
 *
 * @author Ahmed Ashour
 */
@JsxClass
public class MessageChannel extends HtmlUnitScriptable {

    private MessagePort port1_;
    private MessagePort port2_;

    /**
     * Default constructor.
     */
    @JsxConstructor
    public MessageChannel() {
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
