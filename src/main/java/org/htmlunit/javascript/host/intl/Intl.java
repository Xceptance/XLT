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
package org.htmlunit.javascript.host.intl;

import static org.htmlunit.BrowserVersionFeatures.JS_INTL_V8_BREAK_ITERATOR;

import org.htmlunit.BrowserVersion;
import org.htmlunit.corejs.javascript.FunctionObject;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.RecursiveFunctionObject;
import org.htmlunit.javascript.configuration.AbstractJavaScriptConfiguration;
import org.htmlunit.javascript.configuration.ClassConfiguration;

/**
 * A JavaScript object for {@code Intl}.
 *
 * @author Ahmed Ashour
 */
public class Intl extends HtmlUnitScriptable {

    /**
     * Define needed properties.
     * @param browserVersion the browser version
     */
    public void defineProperties(final BrowserVersion browserVersion) {
        define(Collator.class, browserVersion);
        define(DateTimeFormat.class, browserVersion);
        define(NumberFormat.class, browserVersion);
        if (browserVersion.hasFeature(JS_INTL_V8_BREAK_ITERATOR)) {
            define(V8BreakIterator.class, browserVersion);
        }
    }

    private void define(final Class<? extends HtmlUnitScriptable> c, final BrowserVersion browserVersion) {
        try {
            final ClassConfiguration config = AbstractJavaScriptConfiguration.getClassConfiguration(c, browserVersion);
            final HtmlUnitScriptable prototype = JavaScriptEngine.configureClass(config, this, browserVersion);
            final FunctionObject functionObject =
                    new RecursiveFunctionObject(config.getJsConstructor().getKey(),
                            config.getJsConstructor().getValue(), this, browserVersion);
            functionObject.addAsConstructor(this, prototype, ScriptableObject.DONTENUM);
        }
        catch (final Exception e) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
        }
    }
}
