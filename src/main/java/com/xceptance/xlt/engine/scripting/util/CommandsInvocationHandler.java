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
package com.xceptance.xlt.engine.scripting.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.ScriptCommands;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.scripting.TestContext;

/**
 * Handles command invocations.
 */
public class CommandsInvocationHandler<T> implements InvocationHandler
{
    /**
     * Set of all command names.
     */
    private static final HashSet<String> SCRIPT_COMMAND_NAMES = determineScriptCommandNames();

    /**
     * The log facility.
     */
    private final Logger logger;

    /**
     * The wrapped commands implementation.
     */
    private final T commands;

    /**
     * Constructor.
     * 
     * @param commands
     *            the commands implementation
     */
    public CommandsInvocationHandler(final T commands, final Logger logger)
    {
        this.commands = commands;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        // first check whether to abort the current transaction/session
        SessionImpl.getCurrent().checkState();

        // resolve any placeholder in string arguments
        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] instanceof String)
                {
                    args[i] = TestContext.getCurrent().resolve((String) args[i]);
                }
            }
        }

        //
        final String commandString = getCommandString(method, args);
        final boolean isCommand = SCRIPT_COMMAND_NAMES.contains(method.getName());

        // check for any exception
        try
        {
            if (isCommand)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Executing command: " + commandString);
                }
            }

            return doInvokeMethod(commands, method, args);

        }
        catch (final Throwable t)
        {
            Session.getCurrent().setFailed(true);

            if (isCommand)
            {
                // re-throw as is, but add the command to the message text
                ThrowableUtils.prefixMessage(t, String.format("Command failed: %s -> ", commandString));
            }

            // the throwable might origin from a worker thread
            // -> create and assign a new stack trace so that it looks like it was thrown in the main thread
            t.setStackTrace(new Exception().getStackTrace());
            throw t;
        }
    }

    /**
     * Helper method that returns a descriptive string for the given method (aka command) and arguments.
     * 
     * @param method
     *            the method (aka command)
     * @param args
     *            the arguments
     * @return descriptive string for given method and args
     */
    private String getCommandString(Method method, Object[] args)
    {
        String target = null;
        String value = null;

        if (args != null && args.length > 0)
        {
            final boolean isValueOnly = ReplayUtils.isValueOnlyCommand(method.getName());
            if (args[0] != null)
            {
                if (isValueOnly)
                {
                    value = args[0].toString();
                }
                else
                {
                    target = args[0].toString();
                }

                if (args.length > 1 && args[1] != null)
                {
                    value = args[1].toString();
                }
            }
        }

        return String.format("%s target=\"%s\" value=\"%s\"", method.getName(), StringUtils.defaultString(target),
                             StringUtils.defaultString(value));
    }

    /**
     * Invokes the given method.
     */
    protected Object doInvokeMethod(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        try
        {
            return method.invoke(commands, args);
        }
        catch (final InvocationTargetException e)
        {
            // unwrap the causing exception and throw it as is
            throw e.getCause();
        }
    }

    /**
     * Determines all command names (methods declared in interface {@link ScriptCommands}) and returns them as set.
     * 
     * @return set of all command names
     */
    private static HashSet<String> determineScriptCommandNames()
    {
        final HashSet<String> names = new HashSet<>();
        for (final Method m : ScriptCommands.class.getDeclaredMethods())
        {
            names.add(m.getName());
        }
        return names;
    }

    /**
     * Returns the commands implementation.
     * 
     * @return the commands implementation
     */
    public T getCommands()
    {
        return commands;
    }

    /**
     * Returns the command invocation logger.
     * 
     * @return command invocation logger
     */
    public Logger getLogger()
    {
        return logger;
    }
}
