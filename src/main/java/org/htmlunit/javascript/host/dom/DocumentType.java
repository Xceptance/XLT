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

import static org.htmlunit.BrowserVersionFeatures.JS_DOCTYPE_ENTITIES_NULL;
import static org.htmlunit.BrowserVersionFeatures.JS_DOCTYPE_NOTATIONS_NULL;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.Undefined;
import org.htmlunit.html.DomDocumentType;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.w3c.dom.NamedNodeMap;

/**
 * A JavaScript object for {@code DocumentType}.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms762752.aspx">MSDN documentation</a>
 * @see <a href="http://www.xulplanet.com/references/objref/DocumentType.html">XUL Planet</a>
 */
@JsxClass(domClass = DomDocumentType.class)
public class DocumentType extends Node {

    /**
     * Creates an instance.
     */
    public DocumentType() {
    }

    /**
     * Creates an instance.
     */
    @Override
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
        throw JavaScriptEngine.reportRuntimeError("Illegal constructor.");
    }

    /**
     * Returns the name.
     * @return the name
     */
    @JsxGetter
    public String getName() {
        return ((DomDocumentType) getDomNodeOrDie()).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        return getName();
    }

    /**
     * Returns the publicId.
     * @return the publicId
     */
    @JsxGetter
    public String getPublicId() {
        return ((DomDocumentType) getDomNodeOrDie()).getPublicId();
    }

    /**
     * Returns the systemId.
     * @return the systemId
     */
    @JsxGetter
    public String getSystemId() {
        return ((DomDocumentType) getDomNodeOrDie()).getSystemId();
    }

    /**
     * Returns the internal subset.
     * @return the internal subset
     */
    @JsxGetter(IE)
    public String getInternalSubset() {
        final String subset = ((DomDocumentType) getDomNodeOrDie()).getInternalSubset();
        if (StringUtils.isNotEmpty(subset)) {
            return subset;
        }

        return null;
    }

    /**
     * Returns entities.
     * @return entities
     */
    @JsxGetter(IE)
    public Object getEntities() {
        final NamedNodeMap entities = ((DomDocumentType) getDomNodeOrDie()).getEntities();
        if (null != entities) {
            return entities;
        }

        if (getBrowserVersion().hasFeature(JS_DOCTYPE_ENTITIES_NULL)) {
            return null;
        }
        return Undefined.instance;
    }

    /**
     * Returns notations.
     * @return notations
     */
    @JsxGetter(IE)
    public Object getNotations() {
        final NamedNodeMap notations = ((DomDocumentType) getDomNodeOrDie()).getNotations();
        if (null != notations) {
            return notations;
        }

        if (getBrowserVersion().hasFeature(JS_DOCTYPE_NOTATIONS_NULL)) {
            return null;
        }
        return Undefined.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction({CHROME, EDGE, FF, FF_ESR})
    public void remove() {
        super.remove();
    }

    /**
     * Inserts a set of Node or DOMString objects in the children list of this ChildNode's parent,
     * just before this ChildNode.
     * @param context the context
     * @param scope the scope
     * @param thisObj this object
     * @param args the arguments
     * @param function the function
     */
    @JsxFunction({CHROME, EDGE, FF, FF_ESR})
    public static void before(final Context context, final Scriptable scope,
            final Scriptable thisObj, final Object[] args, final Function function) {
        Node.before(context, thisObj, args, function);
    }

    /**
     * Inserts a set of Node or DOMString objects in the children list of this ChildNode's parent,
     * just after this ChildNode.
     * @param context the context
     * @param scope the scope
     * @param thisObj this object
     * @param args the arguments
     * @param function the function
     */
    @JsxFunction({CHROME, EDGE, FF, FF_ESR})
    public static void after(final Context context, final Scriptable scope,
            final Scriptable thisObj, final Object[] args, final Function function) {
        Node.after(context, thisObj, args, function);
    }

    /**
     * Replaces the node wit a set of Node or DOMString objects.
     * @param context the context
     * @param scope the scope
     * @param thisObj this object
     * @param args the arguments
     * @param function the function
     */
    @JsxFunction({CHROME, EDGE, FF, FF_ESR})
    public static void replaceWith(final Context context, final Scriptable scope,
            final Scriptable thisObj, final Object[] args, final Function function) {
        Node.replaceWith(context, thisObj, args, function);
    }
}
