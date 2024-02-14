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
package org.htmlunit.javascript.host.network;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.host.event.EventTarget;

/**
 * A JavaScript object for {@code NetworkInformation}.
 *
 * @author Ronald Brill
 */
@JsxClass({CHROME, EDGE})
public class NetworkInformation extends EventTarget {

    /**
     * Creates an instance.
     */
    public NetworkInformation() {
    }

    /**
     * JavaScript constructor.
     */
    @Override
    @JsxConstructor
    public void jsConstructor() {
        super.jsConstructor();
    }

    /**
     * @return the {@code downlink} property
     */
    @JsxGetter
    public double getDownlink() {
        return 10d;
    }

    /**
     * @return the {@code effectiveType} property
     */
    @JsxGetter
    public String getEffectiveType() {
        return "4g";
    }

    /**
     * @return the {@code rtt} property
     */
    @JsxGetter
    public int getRtt() {
        return 50;
    }
}
