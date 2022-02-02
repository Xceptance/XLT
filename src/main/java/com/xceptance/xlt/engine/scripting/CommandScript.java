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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a test case script read from a script file.
 */
public class CommandScript extends Script
{
    /**
     * 
     */
    private static final Pattern MODULE_PARAMETER_PATTERN = Pattern.compile("(?sm)@@|@\\{[^\\s{}]+\\}");

    /**
     * The script elements (commands, modules) contained in this script.
     */
    private final List<ScriptElement> scriptElements;

    /**
     * The variables
     */
    private final List<String> parameters;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param scriptElements
     *            the script elements
     * @param parameters
     *            the script parameters
     */
    public CommandScript(final File scriptFile, final List<ScriptElement> scriptElements, final List<String> parameters)
    {
        super(scriptFile);
        this.scriptElements = Collections.unmodifiableList(scriptElements);
        this.parameters = parameters;
    }

    /**
     * Returns the value of the 'commands' attribute.
     * 
     * @return the value of commands
     */
    public List<ScriptElement> getScriptElements(final Map<String, String> arguments)
    {
        if (arguments == null || arguments.isEmpty())
        {
            return scriptElements;
        }

        final ArrayList<ScriptElement> scriptElements = new ArrayList<ScriptElement>();
        for (final ScriptElement e : this.scriptElements)
        {
            scriptElements.add(resolve(e, arguments));
        }
        return scriptElements;
    }

    public List<ScriptElement> getScriptElements()
    {
        return getScriptElements(null);
    }

    /**
     * Returns the value of the 'properties' attribute.
     * 
     * @return the value of properties
     */
    public List<String> getParameters()
    {
        return parameters;
    }

    private ScriptElement resolve(final ScriptElement element, final Map<String, String> arguments)
    {
        if (element instanceof Command)
        {
            final Command command = (Command) element;
            return new Command(command.getName(), command.isDisabled(), resolve(command.getTarget(), arguments),
                               resolve(command.getValue(), arguments), element.getLineNumber());
        }

        if (element instanceof ModuleCall)
        {
            final ModuleCall call = (ModuleCall) element;
            final Map<String, String> resolvedParameters = new HashMap<String, String>();
            final CallCondition cond = call.getCondition();
            for (final Map.Entry<String, String> parameter : call.getParameters().entrySet())
            {
                resolvedParameters.put(parameter.getKey(), resolve(parameter.getValue(), arguments));
            }
            return new ModuleCall(call.getName(), call.isDisabled(), cond != null ? new CallCondition(cond.isDisabled(), resolve(cond.getConditionExpression(), arguments)) : null,resolvedParameters, element.getLineNumber());
        }

        return element;
    }

    private String resolve(final String nameOrValue, final Map<String, String> arguments)
    {
        String s = nameOrValue;
        if (StringUtils.isNotBlank(s))
        {
            final Matcher m = MODULE_PARAMETER_PATTERN.matcher(s);
            final StringBuilder sb = new StringBuilder(nameOrValue);
            int offset = 0;
            while (m.find())
            {
                final String match = m.group();

                final String value;
                if (match.length() == 2 && match.charAt(0) == match.charAt(1))
                {
                    value = "@";
                }
                else
                {
                    final String argValue = arguments.get(match.substring(2, match.length() - 1));
                    value = argValue == null ? match : argValue;
                }

                sb.replace(m.start() + offset, m.end() + offset, value);
                offset += value.length() - match.length();
            }

            s = sb.toString();
        }

        return s;
    }
}
