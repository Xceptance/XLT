/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine.scripting;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;

/**
 * Base class of all flow modules.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlUnitActionsModule extends AbstractHtmlUnitScriptModule
{

    /**
     * Executes the flow steps.
     * 
     * @param prevAction
     *            the action to start from
     * @return last performed action
     */
    protected abstract AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable;

    /**
     * Runs the flow.
     * 
     * @param prevAction
     *            the action to start from
     * @return last performed action
     */
    public AbstractHtmlPageAction run(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        TestContext.getCurrent().pushScope(this);
        final String name = getClass().getCanonicalName();
        try
        {
            AbstractCommandAdapter.LOGGER.info("Calling module: " + name);
            return execute(prevAction);
        }
        finally
        {
            AbstractCommandAdapter.LOGGER.info("Returned from module: " + name);
            TestContext.getCurrent().popScope();
        }
    }
}
