/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Script;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

/**
 * An {@link XltScript} wraps an existing {@link Script} instance and holds additional information.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltScript implements Script
{
    /**
     * The wrapped script.
     */
    private final Script script;

    /**
     * The source name associated with the wrapped script.
     */
    private final String sourceName;

    /**
     * Constructor.
     * 
     * @param script
     *            the wrapped script
     * @param sourceName
     *            the source name associated with the wrapped script
     */
    public XltScript(final Script script, final String sourceName)
    {
        this.script = script;
        this.sourceName = sourceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object exec(final Context context, final Scriptable scriptable)
    {
        return script.exec(context, scriptable);
    }

    /**
     * Returns the wrapped script.
     * 
     * @return the wrapped script
     */
    public Script getWrappedScript()
    {
        return script;
    }

    /**
     * Returns the source name associated with the wrapped script.
     * 
     * @return the source name
     */
    public String getSourceName()
    {
        return sourceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return script.toString();
    }
}
