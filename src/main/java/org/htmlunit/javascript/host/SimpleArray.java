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
package org.htmlunit.javascript.host;

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code SimpleArray} allowing access per key and index (like {@link MimeTypeArray}).
 *
 * @author Marc Guillemot
 *
 * @see <a href="http://www.xulplanet.com/references/objref/MimeTypeArray.html">XUL Planet</a>
 */
@JsxClass(isJSObject = false)
public class SimpleArray extends HtmlUnitScriptable {
    private final List<Object> elements_ = new ArrayList<>();

    /**
     * Returns the item at the given index.
     * @param index the index
     * @return the item at the given position
     */
    @JsxFunction
    public Object item(final int index) {
        return elements_.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getWithPreemption(final String name) {
        final Object response = namedItem(name);
        if (response != null) {
            return response;
        }
        return NOT_FOUND;
    }

    /**
     * Returns the element at the specified index, or {@code null} if the index is invalid.
     * {@inheritDoc}
     */
    @Override
    public final Object get(final int index, final Scriptable start) {
        final SimpleArray array = (SimpleArray) start;
        final List<Object> elements = array.elements_;

        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }

    /**
     * Returns the item at the given index.
     * @param name the item name
     * @return the item with the given name
     */
    @JsxFunction
    public Object namedItem(final String name) {
        for (final Object element : elements_) {
            if (name.equals(getItemName(element))) {
                return element;
            }
        }
        return null;
    }

    /**
     * Gets the name of the element.
     * Should be abstract but current implementation of prototype configuration doesn't allow it.
     * @param element the array's element
     * @return the element's name
     */
    protected String getItemName(final Object element) {
        return null;
    }

    /**
     * Gets the array size.
     * @return the number elements
     */
    @JsxGetter
    public int getLength() {
        return elements_.size();
    }

    /**
     * Adds an element.
     * @param element the element to add
     */
    void add(final Object element) {
        elements_.add(element);
    }
}
