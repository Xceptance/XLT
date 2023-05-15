/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.webdriver;

import java.lang.reflect.Method;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.scripting.PageLoadTimeoutException;
import com.xceptance.xlt.engine.scripting.util.CommandsInvocationHandler;

/**
 * An invocation handler that adds cross-cutting functionality to {@link WebDriverScriptCommands} implementations.
 * Currently, this includes:
 * <ul>
 * <li>checking whether to voluntarily abort the current transaction/session</li>
 * <li>retrying a command in case of stale elements</li>
 * <li>ignoring page load timeout exceptions</li>
 * <li>marking the session as failed in case of errors</li>
 * </ul>
 */
public final class WebDriverScriptCommandsInvocationHandler extends CommandsInvocationHandler<WebDriverScriptCommands>
{
    /**
     * The property prefix for scripting properties.
     */
    private static final String PROP_PREFIX_SCRIPTING = XltConstants.XLT_PACKAGE_PATH + ".scripting.";

    /**
     * How often to retry a command in case of a {@link StaleElementReferenceException}.
     */
    private final int commandRetryCount;

    /**
     * Whether to ignore page load timeout exceptions thrown from the scripting layer ("open"/"...AndWait" command
     * timeout) or from Web drivers directly (page-load/script timeout).
     */
    private final boolean ignorePageLoadTimeouts;

    /**
     * Constructor.
     * 
     * @param commands
     *            the commands implementation
     */
    public WebDriverScriptCommandsInvocationHandler(final WebDriverScriptCommands commands, final Logger logger)
    {
        super(commands, logger);

        // get the command retry count
        final XltProperties props = XltProperties.getInstance();
        commandRetryCount = props.getProperty(PROP_PREFIX_SCRIPTING + "commandRetries", 1);

        // whether to ignore page-load timeout exceptions
        ignorePageLoadTimeouts = props.getProperty(PROP_PREFIX_SCRIPTING + "ignorePageLoadTimeouts", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doInvokeMethod(Object proxy, Method method, Object[] args) throws Throwable
    {
        // check for timeout exceptions
        try
        {
            // execute the command, but retry it in case of stale elements
            int retryCount = commandRetryCount;
            while (true)
            {
                // check for stale element exceptions
                try
                {
                    // System.out.printf("### Executing command %s\n", method.getName());
                    return super.doInvokeMethod(proxy, method, args);
                }
                catch (final StaleElementReferenceException e)
                {
                    if (retryCount <= 0)
                    {
                        // no retries left -> pass on the exception
                        throw e;
                    }
                    else
                    {
                        retryCount--;
                        getLogger().debug("Retry command because element was stale: " + e.getMessage());
                    }
                }
            }
        }
        catch (final PageLoadTimeoutException | TimeoutException e)
        {
            if (ignorePageLoadTimeouts)
            {
                getLogger().debug("Ignoring timeout exception: " + e);

                // Note that some script commands actually have a result, but not page-loading commands, so this should
                // be safe.
                return null;
            }
            else
            {
                throw e;
            }
        }
    }
}
