/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import org.htmlunit.WebClient;
import org.htmlunit.javascript.background.DefaultJavaScriptExecutor;

import com.xceptance.xlt.api.engine.Session;

/**
 * Specialization of {@link DefaultJavaScriptExecutor} that renames the executing thread to the session's user ID.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltJavaScriptExecutor extends DefaultJavaScriptExecutor
{
    /**
     * @param webClient
     */
    public XltJavaScriptExecutor(final WebClient webClient)
    {
        super(webClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getThreadName()
    {
        return Session.getCurrent().getUserID() + "-JS";
    }
}
