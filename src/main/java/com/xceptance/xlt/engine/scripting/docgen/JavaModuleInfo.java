/*
 * File: JavaModuleInfo.java
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
