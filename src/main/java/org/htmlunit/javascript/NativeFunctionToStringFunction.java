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
package org.htmlunit.javascript;

import static org.htmlunit.BrowserVersionFeatures.JS_NATIVE_FUNCTION_TOSTRING_COMPACT;
import static org.htmlunit.BrowserVersionFeatures.JS_NATIVE_FUNCTION_TOSTRING_NEW_LINE;
import static org.htmlunit.BrowserVersionFeatures.JS_NATIVE_FUNCTION_TOSTRING_NL;

import org.htmlunit.BrowserVersion;
import org.htmlunit.corejs.javascript.BaseFunction;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.ScriptableObject;

/**
 * Replacement (in fact a wrapper) for Rhino's native toString function on Function prototype
 * allowing to produce the desired formatting.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Ahmed Ashour
 */
public class NativeFunctionToStringFunction extends FunctionWrapper {

    /**
     * Install the wrapper in place of the native toString function on Function's prototype.
     * @param window the scope
     * @param browserVersion the simulated browser
     */
    public static void installFix(final Scriptable window, final BrowserVersion browserVersion) {
        if (browserVersion.hasFeature(JS_NATIVE_FUNCTION_TOSTRING_NEW_LINE)) {
            final ScriptableObject fnPrototype =
                    (ScriptableObject) ScriptableObject.getClassPrototype(window, "Function");
            final Function originalToString = (Function) ScriptableObject.getProperty(fnPrototype, "toString");
            final Function newToString = new NativeFunctionToStringFunction(originalToString);
            ScriptableObject.putProperty(fnPrototype, "toString", newToString);
        }
        else if (browserVersion.hasFeature(JS_NATIVE_FUNCTION_TOSTRING_COMPACT)) {
            final ScriptableObject fnPrototype =
                    (ScriptableObject) ScriptableObject.getClassPrototype(window, "Function");
            final Function originalToString = (Function) ScriptableObject.getProperty(fnPrototype, "toString");
            final Function newToString = new NativeFunctionToStringFunctionChrome(originalToString);
            ScriptableObject.putProperty(fnPrototype, "toString", newToString);
        }
        else if (browserVersion.hasFeature(JS_NATIVE_FUNCTION_TOSTRING_NL)) {
            final ScriptableObject fnPrototype =
                    (ScriptableObject) ScriptableObject.getClassPrototype(window, "Function");
            final Function originalToString = (Function) ScriptableObject.getProperty(fnPrototype, "toString");
            final Function newToString = new NativeFunctionToStringFunctionFF(originalToString);
            ScriptableObject.putProperty(fnPrototype, "toString", newToString);
        }
    }

    NativeFunctionToStringFunction(final Function wrapped) {
        super(wrapped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
        final String s = (String) super.call(cx, scope, thisObj, args);

        if (thisObj instanceof BaseFunction && s.contains("[native code]")) {
            final String functionName = ((BaseFunction) thisObj).getFunctionName();
            return "\nfunction " + functionName + "() {\n    [native code]\n}\n";
        }
        return s.replace("function anonymous() {", "function anonymous() {\n");
    }

    static class NativeFunctionToStringFunctionChrome extends FunctionWrapper {

        NativeFunctionToStringFunctionChrome(final Function wrapped) {
            super(wrapped);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
            final String s = (String) super.call(cx, scope, thisObj, args);

            if (thisObj instanceof BaseFunction && s.contains("[native code]")) {
                final String functionName = ((BaseFunction) thisObj).getFunctionName();
                return "function " + functionName + "() { [native code] }";
            }
            return s.replace("function anonymous() {", "function anonymous(\n) {\n");
        }
    }

    static class NativeFunctionToStringFunctionFF extends FunctionWrapper {

        NativeFunctionToStringFunctionFF(final Function wrapped) {
            super(wrapped);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
            final String s = (String) super.call(cx, scope, thisObj, args);
            return s.replace("function anonymous() {", "function anonymous(\n) {\n");
        }
    }
}
