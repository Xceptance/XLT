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
package org.htmlunit.javascript.host.html;

import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_HAS_SELECT_CLASS_NAME;
import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_IGNORE_NEGATIVE_LENGTH;
import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_IN_ALWAYS_TRUE;
import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_NULL_FOR_OUTSIDE;
import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_REMOVE_IGNORE_IF_INDEX_NEGATIVE;
import static org.htmlunit.BrowserVersionFeatures.JS_SELECT_OPTIONS_REMOVE_THROWS_IF_NEGATIV;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.htmlunit.BrowserVersion;
import org.htmlunit.SgmlPage;
import org.htmlunit.WebAssert;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.ES6Iterator;
import org.htmlunit.corejs.javascript.EvaluatorException;
import org.htmlunit.corejs.javascript.NativeArrayIterator;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.corejs.javascript.Undefined;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.ElementFactory;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;
import org.htmlunit.javascript.configuration.JsxSymbol;
import org.htmlunit.javascript.host.dom.NodeList;

/**
 * This is the array returned by the "options" property of Select.
 *
 * @author David K. Taylor
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author Marc Guillemot
 * @author Daniel Gredler
 * @author Bruce Faulkner
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass({CHROME, EDGE, FF, FF_ESR})
@JsxClass(isJSObject = false, value = IE)
public class HTMLOptionsCollection extends HtmlUnitScriptable {

    private HtmlSelect htmlSelect_;

    /**
     * Creates an instance.
     */
    public HTMLOptionsCollection() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Creates an instance.
     * @param parentScope parent scope
     */
    public HTMLOptionsCollection(final HtmlUnitScriptable parentScope) {
        setParentScope(parentScope);
        setPrototype(getPrototype(getClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        if (getWindow().getWebWindow() != null
                && getBrowserVersion().hasFeature(JS_SELECT_OPTIONS_HAS_SELECT_CLASS_NAME)) {
            return "HTMLSelectElement";
        }
        return super.getClassName();
    }

    /**
     * Initializes this object.
     * @param select the HtmlSelect that this object will retrieve elements from
     */
    public void initialize(final HtmlSelect select) {
        WebAssert.notNull("select", select);
        htmlSelect_ = select;
    }

    /**
     * Returns the object at the specified index.
     *
     * @param index the index
     * @param start the object that get is being called on
     * @return the object or NOT_FOUND
     */
    @Override
    public Object get(final int index, final Scriptable start) {
        if (htmlSelect_ == null || index < 0) {
            return Undefined.instance;
        }

        if (index >= htmlSelect_.getOptionSize()) {
            if (getBrowserVersion().hasFeature(JS_SELECT_OPTIONS_NULL_FOR_OUTSIDE)) {
                return null;
            }
            return Undefined.instance;
        }

        return getScriptableFor(htmlSelect_.getOption(index));
    }

    /**
     * <p>If IE is emulated, and this class does not have the specified property, and the owning
     * select *does* have the specified property, this method delegates the call to the parent
     * select element.</p>
     *
     * @param name {@inheritDoc}
     * @param start {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    public void put(final String name, final Scriptable start, final Object value) {
        if (htmlSelect_ == null) {
            // This object hasn't been initialized; it's probably being used as a prototype.
            // Just pretend we didn't even see this invocation and let Rhino handle it.
            super.put(name, start, value);
            return;
        }

        final HTMLSelectElement parent = htmlSelect_.getScriptableObject();

        if (!has(name, start) && ScriptableObject.hasProperty(parent, name)) {
            ScriptableObject.putProperty(parent, name, value);
        }
        else {
            super.put(name, start, value);
        }
    }

    /**
     * Returns the object at the specified index.
     *
     * @param index the index
     * @return the object or NOT_FOUND
     */
    @JsxFunction
    public Object item(final int index) {
        return get(index, null);
    }

    /**
     * Sets the index property.
     * @param index the index
     * @param start the scriptable object that was originally invoked for this property
     * @param newValue the new value
     */
    @Override
    public void put(final int index, final Scriptable start, final Object newValue) {
        if (newValue == null) {
            // Remove the indexed option.
            htmlSelect_.removeOption(index);
        }
        else {
            final HTMLOptionElement option = (HTMLOptionElement) newValue;
            final HtmlOption htmlOption = (HtmlOption) option.getDomNodeOrNull();
            if (index >= getLength()) {
                setLength(index);
                // Add a new option at the end.
                htmlSelect_.appendOption(htmlOption);
            }
            else {
                // Replace the indexed option.
                htmlSelect_.replaceOption(index, htmlOption);
            }
        }
    }

    /**
     * Returns the number of elements in this array.
     *
     * @return the number of elements in the array
     */
    @JsxGetter
    public int getLength() {
        return htmlSelect_.getOptionSize();
    }

    /**
     * Changes the number of options: removes options if the new length
     * is less than the current one else add new empty options to reach the
     * new length.
     * @param newLength the new length property value
     */
    @JsxSetter
    public void setLength(final int newLength) {
        if (newLength < 0) {
            if (getBrowserVersion().hasFeature(JS_SELECT_OPTIONS_IGNORE_NEGATIVE_LENGTH)) {
                return;
            }
            throw JavaScriptEngine.reportRuntimeError("Length is negative");
        }

        final int currentLength = htmlSelect_.getOptionSize();
        if (currentLength > newLength) {
            htmlSelect_.setOptionSize(newLength);
        }
        else {
            final SgmlPage page = htmlSelect_.getPage();
            final ElementFactory factory = page.getWebClient().getPageCreator()
                                            .getHtmlParser().getFactory(HtmlOption.TAG_NAME);
            for (int i = currentLength; i < newLength; i++) {
                final HtmlOption option = (HtmlOption) factory.createElement(page, HtmlOption.TAG_NAME, null);
                htmlSelect_.appendOption(option);
            }
        }
    }

    /**
     * Adds a new item to the option collection.
     *
     * <p><b><i>Implementation Note:</i></b> The specification for the JavaScript add() method
     * actually calls for the optional newIndex parameter to be an integer. However, the
     * newIndex parameter is specified as an Object here rather than an int because of the
     * way Rhino and HtmlUnit process optional parameters for the JavaScript method calls.
     * If the newIndex parameter were specified as an int, then the Undefined value for an
     * integer is specified as NaN (Not A Number, which is a Double value), but Rhino
     * translates this value into 0 (perhaps correctly?) when converting NaN into an int.
     * As a result, when the newIndex parameter is not specified, it is impossible to make
     * a distinction between a caller of the form add(someObject) and add (someObject, 0).
     * Since the behavior of these two call forms is different, the newIndex parameter is
     * specified as an Object. If the newIndex parameter is not specified by the actual
     * JavaScript code being run, then newIndex is of type org.htmlunit.corejs.javascript.Undefined.
     * If the newIndex parameter is specified, then it should be of type java.lang.Number and
     * can be converted into an integer value.</p>
     *
     * <p>This method will call the {@link #put(int, Scriptable, Object)} method for actually
     * adding the element to the collection.</p>
     *
     * <p>According to <a href="http://msdn.microsoft.com/en-us/library/ms535921.aspx">the
     * Microsoft DHTML reference page for the JavaScript add() method of the options collection</a>,
     * the index parameter is specified as follows:
     * <p>
     * <i>Optional. Integer that specifies the index position in the collection where the element is
     * placed. If no value is given, the method places the element at the end of the collection.</i>
     *
     * @param newOptionObject the DomNode to insert in the collection
     * @param beforeOptionObject An optional parameter which specifies the index position in the
     * collection where the element is placed. If no value is given, the method places
     * the element at the end of the collection.
     *
     * @see #put(int, Scriptable, Object)
     */
    @JsxFunction
    public void add(final Object newOptionObject, final Object beforeOptionObject) {
        final HtmlOption htmlOption = (HtmlOption) ((HTMLOptionElement) newOptionObject).getDomNodeOrNull();

        HtmlOption beforeOption = null;
        // If newIndex was specified, then use it
        if (beforeOptionObject instanceof Number) {
            final int index = ((Integer) Context.jsToJava(beforeOptionObject, Integer.class)).intValue();
            if (index < 0 || index >= getLength()) {
                // Add a new option at the end.
                htmlSelect_.appendOption(htmlOption);
                return;
            }

            beforeOption = (HtmlOption) ((HTMLOptionElement) item(index)).getDomNodeOrDie();
        }
        else if (beforeOptionObject instanceof HTMLOptionElement) {
            beforeOption = (HtmlOption) ((HTMLOptionElement) beforeOptionObject).getDomNodeOrDie();
            if (beforeOption.getParentNode() != htmlSelect_) {
                throw new EvaluatorException("Unknown option.");
            }
        }

        if (null == beforeOption) {
            htmlSelect_.appendOption(htmlOption);
            return;
        }

        beforeOption.insertBefore(htmlOption);
    }

    /**
     * Removes the option at the specified index.
     * @param index the option index
     */
    @JsxFunction
    public void remove(final int index) {
        int idx = index;
        final BrowserVersion browser = getBrowserVersion();
        if (idx < 0) {
            if (browser.hasFeature(JS_SELECT_OPTIONS_REMOVE_IGNORE_IF_INDEX_NEGATIVE)) {
                return;
            }
            if (index < 0 && getBrowserVersion().hasFeature(JS_SELECT_OPTIONS_REMOVE_THROWS_IF_NEGATIV)) {
                throw JavaScriptEngine.reportRuntimeError("Invalid index for option collection: " + index);
            }
        }

        idx = Math.max(idx, 0);
        if (idx >= getLength()) {
            return;
        }

        htmlSelect_.removeOption(idx);
    }

    @Override
    public boolean has(final int index, final Scriptable start) {
        if (getBrowserVersion().hasFeature(JS_SELECT_OPTIONS_IN_ALWAYS_TRUE)) {
            return true;
        }

        return super.has(index, start);
    }

    /**
     * Returns the value of the {@code selectedIndex} property.
     * @return the {@code selectedIndex} property
     */
    @JsxGetter
    public int getSelectedIndex() {
        return htmlSelect_.getSelectedIndex();
    }

    /**
     * Sets the value of the {@code selectedIndex} property.
     * @param index the new value
     */
    @JsxSetter
    public void setSelectedIndex(final int index) {
        htmlSelect_.setSelectedIndex(index);
    }

    /**
     * Returns the child nodes of the current element.
     * @return the child nodes of the current element
     */
    @JsxGetter(IE)
    public NodeList getChildNodes() {
        final NodeList childNodes = new NodeList(htmlSelect_, false);
        childNodes.setElementsSupplier(
                (Supplier<List<DomNode>> & Serializable)
                () -> {
                    final List<DomNode> response = new ArrayList<>();
                    for (final DomNode child : htmlSelect_.getChildren()) {
                        response.add(child);
                    }

                    return response;
                });
        return childNodes;
    }

    @JsxSymbol({CHROME, EDGE, FF, FF_ESR})
    public ES6Iterator iterator() {
        return new NativeArrayIterator(getParentScope(), this, NativeArrayIterator.ARRAY_ITERATOR_TYPE.VALUES);
    }
}
