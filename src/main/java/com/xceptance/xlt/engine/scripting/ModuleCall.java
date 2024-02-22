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

import java.util.Collections;
import java.util.Map;

/**
 * Represents a module call in a script file.
 */
public class ModuleCall extends ScriptElement
{
    /**
     * The parameters to pass to the module when the module is executed.
     */
    private final Map<String, String> parameters;

    /** 
     * The condition that must evaluate to <code>true</code> in order to execute this module call. 
     */
    private final CallCondition condition;

    /**
     * Constructor.
     * 
     * @param name
     *            the module name
     * @param disabled
     *            whether this module is disabled (i.e. commented out)
     * @param condition
     *            the condition for this module call
     * @param parameters
     *            the module parameters
     */
    public ModuleCall(final String name, final boolean disabled, final CallCondition callCondition, final Map<String, String> parameters,
                      final int lineNumber)
    {
        super(name, disabled, lineNumber);
        this.condition = callCondition;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns the parameters to pass to the module when the module is executed.
     * 
     * @return the module parameters
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("module %s", getName());
    }
    
    public boolean hasCondition()
    {
        return condition != null && !condition.isDisabled();
    }
    
    public CallCondition getCondition()
    {
        return condition;
    }
}
