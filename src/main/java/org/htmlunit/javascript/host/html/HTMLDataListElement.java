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

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import java.io.Serializable;
import java.util.function.Predicate;

import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlDataList;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * The JavaScript object {@code HTMLDataListElement}.
 *
 * @author Ronald Brill
 * @author Ahmed Ashour
 */
@JsxClass(domClass = HtmlDataList.class)
public class HTMLDataListElement extends HTMLElement {

    private HTMLCollection options_;

    /**
     * Creates an instance.
     */
    public HTMLDataListElement() {
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
     * Returns the {@code options} attribute.
     * @return the {@code options} attribute
     */
    @JsxGetter
    public Object getOptions() {
        if (options_ == null) {
            options_ = new HTMLCollection(getDomNodeOrDie(), false);
            options_.setIsMatchingPredicate(
                    (Predicate<DomNode> & Serializable)
                    node -> node instanceof HtmlOption);
        }
        return options_;
    }

}
