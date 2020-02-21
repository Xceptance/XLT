/*
 * File: PackageInfo.java
 * Created on: Nov 21, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting.docgen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Script-package information.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("package")
public class PackageInfo implements Comparable<PackageInfo>
{
    final String name;

    @XStreamAlias("test-data")
    private Map<String, String> testData;

    @XStreamOmitField
    private final Set<TestScriptInfo> tests = new HashSet<TestScriptInfo>();

    @XStreamOmitField
    private final Set<ModuleScriptInfo> scriptModules = new HashSet<ModuleScriptInfo>();

    @XStreamOmitField
    private final Set<JavaModuleInfo> javaModules = new HashSet<JavaModuleInfo>();

    PackageInfo()
    {
        this(null);
    }

    PackageInfo(final String pkgName)
    {
        name = StringUtils.defaultString(pkgName);
    }

    public boolean isDefaultPackage()
    {
        return "".equals(name);
    }

    void setTestData(final Map<String, String> data)
    {
        this.testData = data == null ? Collections.<String, String>emptyMap() : new TreeMap<String, String>(data);
    }

    public Map<String, String> getTestData()
    {
        return testData;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    public void addScript(final BaseInfo script)
    {
        if (script instanceof TestScriptInfo)
        {
            tests.add((TestScriptInfo) script);
        }
        else if (script instanceof ModuleScriptInfo)
        {
            scriptModules.add((ModuleScriptInfo) script);
        }
        else if (script instanceof JavaModuleInfo)
        {
            javaModules.add((JavaModuleInfo) script);
        }
        else
        {
            throw new IllegalArgumentException("Don't know how to handle class: " + getClass().getCanonicalName());
        }
    }

    /**
     * @return the tests
     */
    public Set<TestScriptInfo> getTests()
    {
        return Collections.unmodifiableSet(tests);
    }

    /**
     * @return the scriptModules
     */
    public Set<ModuleScriptInfo> getScriptModules()
    {
        return Collections.unmodifiableSet(scriptModules);
    }

    /**
     * @return the javaModules
     */
    public Set<JavaModuleInfo> getJavaModules()
    {
        return Collections.unmodifiableSet(javaModules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PackageInfo o)
    {
        return getName().compareToIgnoreCase(o.getName());
    }

    public Set<BaseInfo> getModules()
    {
        final Set<BaseInfo> s = new HashSet<BaseInfo>();
        s.addAll(scriptModules);
        s.addAll(javaModules);

        return Collections.unmodifiableSet(s);
    }

    /**
     * @return
     */
    public int size()
    {
        return tests.size() + scriptModules.size() + javaModules.size();
    }
}
