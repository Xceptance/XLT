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

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code MimeType}.
 *
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Ronald Brill
 *
 * @see <a href="http://www.xulplanet.com/references/objref/MimeType.html">XUL Planet</a>
 */
@JsxClass
public class MimeType extends HtmlUnitScriptable {

    private String description_;
    private String suffixes_;
    private String type_;
    private Plugin enabledPlugin_;

    /**
     * Creates an instance.
     */
    public MimeType() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Constructor initializing fields.
     * @param type the mime type
     * @param description the type description
     * @param suffixes the file suffixes
     * @param plugin the associated plugin
     */
    public MimeType(final String type, final String description, final String suffixes, final Plugin plugin) {
        type_ = type;
        description_ = description;
        suffixes_ = suffixes;
        enabledPlugin_ = plugin;
    }

    /**
     * Returns the mime type's description.
     * @return the description
     */
    @JsxGetter
    public String getDescription() {
        return description_;
    }

    /**
     * Returns the mime type's suffixes.
     * @return the suffixes
     */
    @JsxGetter
    public String getSuffixes() {
        return suffixes_;
    }

    /**
     * Returns the mime type's suffixes.
     * @return the suffixes
     */
    @JsxGetter
    public String getType() {
        return type_;
    }

    /**
     * Returns the mime type's associated plugin.
     * @return the plugin
     */
    @JsxGetter
    public Object getEnabledPlugin() {
        return enabledPlugin_;
    }
}
