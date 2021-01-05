/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

/**
 * Represents a command read from a script file.
 */
public class Command extends ScriptElement
{
    /**
     * The "target" parameter of the command.
     */
    private final String target;

    /**
     * The "value" parameter of the command.
     */
    private final String value;

    /**
     * Constructor.
     * 
     * @param name
     *            the name
     * @param disabled
     *            whether this command is disabled (i.e. commented out)
     * @param target
     *            the target
     * @param value
     *            the value
     */
    public Command(final String name, final boolean disabled, final String target, final String value, final int lineNumber)
    {
        super(name, disabled, lineNumber);
        this.target = target;
        this.value = value;
    }

    /**
     * Returns the value of the 'target' attribute.
     * 
     * @return the value of target
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * Returns the value of the 'value' attribute.
     * 
     * @return the value of value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("command %s target=\"%s\" value=\"%s\"", getName(), target, value);
    }
}
