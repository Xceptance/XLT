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

import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.debug.DebugFrame;

/**
 * An adapter class for debug frame implementations. The methods in this class are empty. This class
 * exists as a convenience for creating debug frame objects.
 *
 * @author Daniel Gredler
 */
public class DebugFrameAdapter implements DebugFrame {

    /** {@inheritDoc} */
    @Override
    public void onDebuggerStatement(final Context cx) {
        // Empty.
    }

    /** {@inheritDoc} */
    @Override
    public void onEnter(final Context cx, final Scriptable activation, final Scriptable thisObj, final Object[] args) {
        // Empty.
    }

    /** {@inheritDoc} */
    @Override
    public void onExceptionThrown(final Context cx, final Throwable ex) {
        // Empty.
    }

    /** {@inheritDoc} */
    @Override
    public void onExit(final Context cx, final boolean byThrow, final Object resultOrException) {
        // Empty.
    }

    /** {@inheritDoc} */
    @Override
    public void onLineChange(final Context cx, final int lineNumber) {
        // Empty.
    }

}
