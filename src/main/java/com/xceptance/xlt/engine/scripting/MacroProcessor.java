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
package com.xceptance.xlt.engine.scripting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Macro processor.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class MacroProcessor
{
    /**
     * Mapping of macro names to the appropriate macros.
     */
    private final Map<String, Macro> macros;

    /**
     * Macro pattern used to parse the name and arguments of a macro statement.
     */
    private final Pattern MACRO_PATTERN = Pattern.compile("^(.+?)\\((.*?)\\)$");

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private MacroProcessor()
    {
        macros = new HashMap<String, Macro>();
        macros.put("NOW", new Macro()
        {
            @Override
            public String execute(final String... arguments)
            {
                return Long.toString(new Date().getTime());
            }
        });
        macros.put("RANDOM.String", new Macro()
        {
            @Override
            public String execute(final String... arguments)
            {
                final String chars = "abcdefghijklmnopqrstuvwxyz";
                int length = 0;
                if (arguments.length > 0)
                {
                    try
                    {
                        length = Integer.parseInt(arguments[0]);
                    }
                    catch (final Exception e)
                    {
                        // ignore
                    }
                }
                if (length < 0)
                {
                    length = 0;
                }

                final StringBuilder sb = new StringBuilder(length);
                for (int i = 0; i < length; i++)
                {
                    sb.append(chars.charAt(XltRandom.nextInt(chars.length())));
                }
                return sb.toString();
            }

        });
        macros.put("RANDOM.Number", new Macro()
        {
            @Override
            public String execute(final String... arguments)
            {
                if (arguments != null && arguments.length > 0)
                {
                    int arg0 = 0;
                    try
                    {
                        arg0 = Math.max(arg0, Integer.parseInt(arguments[0]));
                    }
                    catch (final NumberFormatException e)
                    {
                        // ignore
                    }

                    if (arguments.length == 2)
                    {
                        int max = 0;
                        try
                        {
                            max = Math.max(arg0, Integer.parseInt(arguments[1]));
                        }
                        catch (final NumberFormatException e)
                        {
                            // ignore
                        }

                        return Integer.toString(XltRandom.nextInt(arg0, max));
                    }
                    else if (arguments.length == 1)
                    {
                        return Integer.toString(XltRandom.nextInt(arg0));
                    }
                }
                return "";
            }
        });
    }

    /**
     * Returns whether or not the given macro statement is valid.
     * 
     * @param macroStatement
     *            the macro statement to be checked
     * @return <tt>true</tt> if the given macro statement is valid, <tt>false</tt> otherwise
     */
    public boolean isMacro(final String macroStatement)
    {
        return macros.containsKey(getMacroName(macroStatement));
    }

    /**
     * Executes the given macro statement.
     * 
     * @param macroStatement
     *            the macro statement to execute
     * @return result of executing the given macro statement
     */
    public String executeMacro(final String macroStatement)
    {
        final Macro macro = macros.get(getMacroName(macroStatement));
        if (macro == null)
        {
            throw new IllegalArgumentException("Macro statement '" + macroStatement + "' could not be recognized as valid macro");
        }
        return macro.execute(getMacroArguments(macroStatement));
    }

    /**
     * Returns the macro processor instance.
     * 
     * @return processor instance
     */
    public static MacroProcessor getInstance()
    {
        return Singleton_Holder._INSTANCE;
    }

    /**
     * Returns the name of the used macro in the given macro statement.
     * 
     * @param macroStatement
     *            the macro statement
     * @return name of used macro
     */
    private String getMacroName(final String macroStatement)
    {
        final Matcher m = MACRO_PATTERN.matcher(macroStatement);
        if (m.matches())
        {
            return m.group(1);
        }
        return macroStatement;
    }

    /**
     * Returns the arguments of the used macro in the given macro statement.
     * 
     * @param macroStatement
     *            the macro statement
     * @return macro arguments
     */
    private String[] getMacroArguments(final String macroStatement)
    {
        final Matcher m = MACRO_PATTERN.matcher(macroStatement);
        if (m.matches())
        {
            final String[] args = m.group(2).split(",");
            for (int i = 0; i < args.length; i++)
            {
                args[i] = args[i].trim();
            }

            return args;
        }
        return new String[0];
    }

    private static class Singleton_Holder
    {
        private static final MacroProcessor _INSTANCE = new MacroProcessor();
    }

    /**
     * Macro.
     */
    private abstract class Macro
    {
        /**
         * Execute this macro using the given arguments.
         * 
         * @param arguments
         *            the arguments
         * @return result of executing this macro using the given arguments
         */
        public abstract String execute(final String... arguments);
    }
}
