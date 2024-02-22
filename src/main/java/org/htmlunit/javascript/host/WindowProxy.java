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

import org.htmlunit.WebWindow;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.javascript.HtmlUnitScriptableProxy;

/**
 * Proxy for a {@link Window} script object.
 *
 * @author Marc Guillemot
 */
public class WindowProxy extends HtmlUnitScriptableProxy<Window> {

    private final WebWindow webWindow_;

    /**
     * Construct a proxy for the {@link Window} of the {@link WebWindow}.
     * @param webWindow the window
     */
    public WindowProxy(final WebWindow webWindow) {
        webWindow_ = webWindow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Window getDelegee() {
        return webWindow_.getScriptableObject();
    }

    /**
     * Does nothing.
     * @param parent the new parent scope
     */
    @Override
    public void setParentScope(final Scriptable parent) {
        // nothing as the window is the top level scope and its parent scope should stay null
    }
}
