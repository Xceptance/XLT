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
package org.htmlunit.javascript.host.file;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code FileList}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass
public class FileList extends HtmlUnitScriptable {

    private File[] files_;

    /**
     * Creates an instance.
     */
    public FileList() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Creates a new instance.
     * @param array the array of files
     */
    public FileList(final java.io.File[] array) {
        files_ = new File[array.length];

        for (int i = 0; i < array.length; i++) {
            files_[i] = new File(array[i].getAbsolutePath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentScope(final Scriptable m) {
        super.setParentScope(m);
        if (files_ != null) {
            for (final File file : files_) {
                file.setParentScope(m);
                file.setPrototype(getPrototype(file.getClass()));
            }
        }
    }

    /**
     * Returns the {@code length} property.
     * @return the {@code length} property
     */
    @JsxGetter
    public int getLength() {
        return files_.length;
    }

    /**
     * Returns a {@code File} object representing the file at the specified index in the file list.
     * @param index The zero-based index of the file to retrieve from the list
     * @return The {@code File} representing the requested file
     */
    @JsxFunction
    public File item(final int index) {
        return files_[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final int index, final Scriptable start) {
        return item(index);
    }
}
