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

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * A JavaScript object for {@code Namespace}.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535854.aspx">MSDN documentation</a>
 */
@JsxClass(isJSObject = false, value = IE)
public class Namespace extends HtmlUnitScriptable {

    private String name_;
    private String urn_;

    /**
     * Creates an instance.
     */
    public Namespace() {
        // Empty.
    }

    /**
     * Creates an instance.
     *
     * @param parentScope parent scope
     * @param name the name
     * @param urn the Uniform Resource Name
     */
    public Namespace(final ScriptableObject parentScope, final String name, final String urn) {
        setParentScope(parentScope);
        setPrototype(getPrototype(getClass()));
        name_ = name;
        urn_ = urn;
    }

    /**
     * Retrieves the name of the namespace.
     * @return the name
     */
    @JsxGetter
    public String getName() {
        return name_;
    }

    /**
     * Gets a Uniform Resource Name (URN) for a target document.
     * @return the URN
     */
    @JsxGetter
    public String getUrn() {
        return urn_;
    }

    /**
     * Gets a Uniform Resource Name (URN) for a target document.
     * @param urn the Uniform Resource Name
     */
    @JsxSetter
    public void setUrn(final String urn) {
        urn_ = urn;
    }
}
