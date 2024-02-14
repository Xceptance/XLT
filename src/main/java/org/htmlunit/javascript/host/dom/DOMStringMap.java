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
package org.htmlunit.javascript.host.dom;

import static org.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.host.Window;
import org.htmlunit.util.StringUtils;

/**
 * A JavaScript object for {@code DOMStringMap}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public final class DOMStringMap extends HtmlUnitScriptable {

    /**
     * Creates an instance.
     */
    public DOMStringMap() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Creates an instance.
     * @param node the node which contains the underlying string
     */
    public DOMStringMap(final Node node) {
        setDomNode(node.getDomNodeOrDie(), false);
        setParentScope(node.getParentScope());
        setPrototype(getPrototype(getClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final String name, final Scriptable start) {
        final HtmlElement e = (HtmlElement) getDomNodeOrNull();
        if (e != null) {
            final String value = e.getAttribute("data-" + StringUtils.cssDeCamelize(name));
            if (ATTRIBUTE_NOT_DEFINED != value) {
                return value;
            }
        }
        return NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String name, final Scriptable start, final Object value) {
        if (!(ScriptableObject.getTopLevelScope(this) instanceof Window) || getWindow().getWebWindow() == null) {
            super.put(name, start, value);
        }
        else {
            final HtmlElement e = (HtmlElement) getDomNodeOrNull();
            if (e != null) {
                e.setAttribute("data-" + StringUtils.cssDeCamelize(name), JavaScriptEngine.toString(value));
            }
        }
    }
}
