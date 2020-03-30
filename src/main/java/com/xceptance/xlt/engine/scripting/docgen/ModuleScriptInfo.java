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
package com.xceptance.xlt.engine.scripting.docgen;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Module script information.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("module")
public class ModuleScriptInfo extends ScriptInfo
{
    @XStreamImplicit
    private final List<ModuleParameterInfo> parameters = new ArrayList<ModuleParameterInfo>();

    @XStreamAlias("used")
    private boolean called;

    /**
     * @param name
     */
    ModuleScriptInfo(String name)
    {
        super(name);
    }

    ModuleScriptInfo(final String name, final String id)
    {
        super(name, id);
    }

    void addParameter(final String name, final String description)
    {
        parameters.add(new ModuleParameterInfo(name, description));
    }

    /**
     * @return the parameters
     */
    public List<ModuleParameterInfo> getParameters()
    {
        return parameters;
    }

    void setCalled(final boolean isCalled)
    {
        called = isCalled;
    }

    public boolean isCalled()
    {
        return called;
    }
}
