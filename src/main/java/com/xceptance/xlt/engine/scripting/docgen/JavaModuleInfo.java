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
package com.xceptance.xlt.engine.scripting.docgen;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Java module information.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("javamodule")
public class JavaModuleInfo extends BaseInfo
{
    @XStreamImplicit
    private final List<ModuleParameterInfo> parameters = new ArrayList<ModuleParameterInfo>();

    @XStreamAlias("java-class")
    final String implClassName;

    @XStreamAlias("used")
    private boolean called;

    JavaModuleInfo(final String name, final String implClassName)
    {
        super(name);
        this.implClassName = implClassName;
    }

    JavaModuleInfo(final String name, final String id, final String implClassName)
    {
        super(name, id);
        this.implClassName = implClassName;
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

    /**
     * @return the implClassName
     */
    public String getImplClassName()
    {
        return implClassName;
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
