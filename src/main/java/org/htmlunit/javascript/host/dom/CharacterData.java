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

import static org.htmlunit.BrowserVersionFeatures.JS_DOM_CDATA_DELETE_THROWS_NEGATIVE_COUNT;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.html.DomCharacterData;
import org.htmlunit.html.DomElement;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;
import org.htmlunit.javascript.host.Element;

/**
 * A JavaScript object for {@code CharacterData}.
 *
 * @author David K. Taylor
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class CharacterData extends Node {

    /**
     * Creates an instance.
     */
    public CharacterData() {
    }

    /**
     * JavaScript constructor.
     */
    @Override
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
        super.jsConstructor();
    }

    /**
     * Gets the JavaScript property {@code data} for this character data.
     * @return the String of data
     */
    @JsxGetter
    public Object getData() {
        return getDomCharacterDataOrDie().getData();
    }

    /**
     * Sets the JavaScript property {@code data} for this character data.
     * @param newValue the new String of data
     */
    @JsxSetter
    public void setData(final String newValue) {
        getDomCharacterDataOrDie().setData(newValue);
    }

    /**
     * Gets the number of character in the character data.
     * @return the number of characters
     */
    @JsxGetter
    public int getLength() {
        return getDomCharacterDataOrDie().getLength();
    }

    /**
     * Append a string to character data.
     * @param arg the string to be appended to the character data
     */
    @JsxFunction
    public void appendData(final String arg) {
        getDomCharacterDataOrDie().appendData(arg);
    }

    /**
     * Delete characters from character data.
     * @param offset the position of the first character to be deleted
     * @param count the number of characters to be deleted
     */
    @JsxFunction
    public void deleteData(final int offset, final int count) {
        if (offset < 0) {
            throw JavaScriptEngine.reportRuntimeError("Provided offset: " + offset + " is less than zero.");
        }

        if (getBrowserVersion().hasFeature(JS_DOM_CDATA_DELETE_THROWS_NEGATIVE_COUNT)) {
            if (count < 0) {
                throw JavaScriptEngine.reportRuntimeError("Provided count: " + count + " is less than zero.");
            }
            if (count == 0) {
                return;
            }
        }

        final DomCharacterData domCharacterData = getDomCharacterDataOrDie();
        if (offset > domCharacterData.getLength()) {
            throw JavaScriptEngine.reportRuntimeError("Provided offset: " + offset + " is greater than length.");
        }

        domCharacterData.deleteData(offset, count);
    }

    /**
     * Insert a string into character data.
     * @param offset the position within the first character at which
     * the string is to be inserted.
     * @param arg the string to insert
     */
    @JsxFunction
    public void insertData(final int offset, final String arg) {
        getDomCharacterDataOrDie().insertData(offset, arg);
    }

    /**
     * Replace characters of character data with a string.
     * @param offset the position within the first character at which
     * the string is to be replaced.
     * @param count the number of characters to be replaced
     * @param arg the string that replaces the count characters beginning at
     * the character at offset.
     */
    @JsxFunction
    public void replaceData(final int offset, final int count, final String arg) {
        getDomCharacterDataOrDie().replaceData(offset, count, arg);
    }

    /**
     * Extract a substring from character data.
     * @param offset the position of the first character to be extracted
     * @param count the number of characters to be extracted
     * @return a string that consists of the count characters of the character
     *         data starting from the character at position offset
     */
    @JsxFunction
    public String substringData(final int offset, final int count) {
        return getDomCharacterDataOrDie().substringData(offset, count);
    }

    private DomCharacterData getDomCharacterDataOrDie() {
        return (DomCharacterData) super.getDomNodeOrDie();
    }

    /**
     * Returns the next element sibling.
     * @return the next element sibling
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public Element getNextElementSibling() {
        final DomElement child = getDomNodeOrDie().getNextElementSibling();
        if (child != null) {
            return child.getScriptableObject();
        }
        return null;
    }

    /**
     * Returns the previous element sibling.
     * @return the previous element sibling
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public Element getPreviousElementSibling() {
        final DomElement child = getDomNodeOrDie().getPreviousElementSibling();
        if (child != null) {
            return child.getScriptableObject();
        }
        return null;
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
            final Scriptable thisObj, final Object[] args,  final Function function) {
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
