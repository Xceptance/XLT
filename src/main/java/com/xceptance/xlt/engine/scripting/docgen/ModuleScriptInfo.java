/*
 * File: ModuleScriptInfo.java
 * Created on: Nov 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
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
