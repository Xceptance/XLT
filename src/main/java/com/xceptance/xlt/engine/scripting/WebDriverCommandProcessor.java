/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.engine.scripting.ScriptCommands;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverCommandAdapter;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

/**
 * A command processor which delegates commands down to an underlying WebDriver instance.
 */
public class WebDriverCommandProcessor
{
    /**
     * The command implementation.
     */
    private final WebDriverScriptCommands commands;

    /**
     * Maps the method signature to the method objects.
     */
    private static final HashMap<String, Method[]> methodMap = new HashMap<String, Method[]>();

    /**
     * Names of methods that are not interpreted.
     */
    private static final String[] notInterpretedMethods = new String[]
        {
            "startAction"
        };

    static
    {
        prepareMethodMap();
    }

    /**
     * Constructor.
     * 
     * @param driver
     *            the web driver instance to use
     * @param baseUrl
     *            the base URL
     */
    public WebDriverCommandProcessor(final WebDriver driver)
    {
        String baseUrl = TestContext.getCurrent().getBaseUrl();
        if (baseUrl.endsWith("/"))
        {
            // cut off the trailing slash
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        commands = WebDriverCommandAdapter.createInstance(driver, baseUrl);
    }

    /**
     * Closes this command processor.
     */
    public void close()
    {
    }

    /**
     * Returns the underlying web driver instance.
     * 
     * @return the instance of {@link WebDriver} that this processor is wrapping
     */
    public WebDriver getUnderlyingWebDriver()
    {
        return commands.getUnderlyingWebDriver();
    }

    /**
     * Runs the given command with the specified parameters.
     * 
     * @param commandName
     *            the command's name
     * @param param1
     *            the first parameter
     * @param param2
     *            the second parameter
     * @throws Throwable
     *             thrown in case of assertion errors or script failures
     */
    public void doCommand(final String commandName, final String param1, final String param2) throws Throwable
    {

        final Method method = getMethod(commandName, param1, param2);
        final Object[] args = castArgumentsIfNecessary(method, new String[]
            {
                param1, param2
            });

        // invoke the method
        try
        {
            method.invoke(commands, args);
        }
        catch (final SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            throw new UnsupportedOperationException("Unknown command: " + commandName);
        }
        catch (final InvocationTargetException e)
        {
            throw e.getCause();
        }

    }

    /**
     * Prepares the map with all methods included in the ScriptCommands-Interface
     */
    private static void prepareMethodMap()
    {
        final Method scMethods[] = ScriptCommands.class.getDeclaredMethods();
        for (final Method method : scMethods)
        {
            boolean skip = false;
            for (final String s : notInterpretedMethods)
            {
                if (s.compareTo(method.getName()) == 0)
                {
                    skip = true;
                    break;
                }
            }
            // script commands cannot handle more than 2 parameters
            if (skip || method.getParameterCount() > 2)
            {
                continue;
            }
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final String currentName = method.getName();

            try
            {
                final Method adapterMethod = WebDriverScriptCommands.class.getMethod(currentName, parameterTypes);
                Method[] methods = methodMap.get(currentName);
                if (methods != null)
                {
                    final Method[] clone = new Method[methods.length + 1];
                    System.arraycopy(methods, 0, clone, 1, methods.length);
                    methods = clone;
                }
                else
                {
                    methods = new Method[1];
                }
                methods[0] = adapterMethod;
                methodMap.put(currentName, methods);
            }
            catch (final NoSuchMethodException nsme)
            {
                throw new RuntimeException(String.format("Could not find declared method '%s' in class '%s'", currentName,
                                                         WebDriverCommandAdapter.class.getName()), nsme);
            }
        }
    }

    /**
     * Casts the given arguments if necessary.
     * 
     * @param method
     *            the method
     * @param params
     *            the method parameters to cast
     * @return the probably casted arguments
     */
    private static Object[] castArgumentsIfNecessary(final Method method, final String[] params)
    {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object args[] = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
        {
            final Class<?> clazz = parameterTypes[i];
            if (clazz.equals(int.class))
            {
                args[i] = Integer.parseInt(params[i]);
            }
            else if (clazz.equals(long.class))
            {
                args[i] = Long.parseLong(params[i]);
            }
            else if (clazz.equals(String.class))
            {
                args[i] = params[i];
            }
            else
            {
                throw new IllegalArgumentException("Unsupported parameter type: " + clazz.getCanonicalName());
            }
        }

        return args;
    }

    /**
     * Determines the method for the given command and parameters.
     * 
     * @param commandName
     *            the command name
     * @param param1
     *            the 1st parameter
     * @param param2
     *            the 2nd parameter
     * @return the method to invoke
     */
    private static Method getMethod(final String commandName, final String param1, final String param2)
    {
        final Method[] methods = methodMap.get(commandName);
        if (methods != null)
        {
            if (methods.length == 1)
            {
                return methods[0];
            }

            final int nbParams = getParamCount(param1, param2);
            for (final Method method : methods)
            {
                if (method.getParameterTypes().length == nbParams)
                {
                    return method;
                }
            }
        }

        throw new UnsupportedOperationException("Unknown command: " + commandName);
    }

    /**
     * Returns the number of parameters actually used.
     * 
     * @param param1
     *            the 1st parameter
     * @param param2
     *            the 2nd parameter
     * @return number of parameters actually used (not blank)
     */
    private static int getParamCount(final String param1, final String param2)
    {
        int nbParams = 0;
        if (StringUtils.isNotBlank(param1))
        {
            ++nbParams;
            if (StringUtils.isNotBlank(param2))
            {
                ++nbParams;
            }
        }
        return nbParams;
    }

    WebDriverScriptCommands getAdapter()
    {
        return commands;
    }
}
