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
package com.xceptance.xlt.engine.scripting.docgen;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.engine.scripting.TestDataUtils;
import com.xceptance.xlt.engine.util.ScriptingUtils;

/**
 * Test suite information.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("testsuite")
public class TestSuiteInfo
{
    /** The suite's name. */
    String name;

    /** The suite's description (raw). */
    String description;

    /** The test-scripts of this suite. */
    private final Set<TestScriptInfo> tests = new HashSet<TestScriptInfo>();

    /** The script-modules of this suite. */
    private final Set<ModuleScriptInfo> modules = new HashSet<ModuleScriptInfo>();

    /** The java-modules of this suite. */
    @XStreamAlias("java-modules")
    private final Set<JavaModuleInfo> javaModules = new HashSet<JavaModuleInfo>();

    /** Maps script-package names to their descriptor. */
    private final Map<String, PackageInfo> packages = new HashMap<String, PackageInfo>();

    /** Global test data. */
    @XStreamAlias("global-testdata")
    private final Map<String, String> globalTestData;

    /** The markup text generated from the suite's description. */
    private transient String descriptionMarkup;

    TestSuiteInfo(final File suitePath)
    {
        name = suitePath.getName();
        globalTestData = readGlobalTestData(suitePath);
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the descriptionMarkup
     */
    public String getDescriptionMarkup()
    {
        if (descriptionMarkup == null)
        {
            descriptionMarkup = Marked.getInstance().markdownToHTML(description);
        }

        return descriptionMarkup;
    }

    /**
     * Reads the properties file <code>&lt;suitePath&gt;/global_testdata.properties</code> and returns its data as map.
     * 
     * @param suitePath
     *            the path to the test suite
     * @return global test data defined in <code>&lt;suitePath&gt;/global_testdata.properties</code> or an empty map in
     *         case the file does not exist or could not be read
     */
    private Map<String, String> readGlobalTestData(final File suitePath)
    {
        final XltExecutionContext ctx = XltExecutionContext.getCurrent();
        final File f = ctx.getTestSuiteHomeDirAsFile();
        Map<String, String> data;

        try
        {
            ctx.setTestSuiteHomeDir(suitePath);
            data = TestDataUtils.getGlobalTestData();
            if (!data.isEmpty())
            {
                data = new TreeMap<String, String>(data);
            }
        }
        finally
        {
            ctx.setTestSuiteHomeDir(f);
        }

        return data;
    }

    /**
     * Adds the given information to the appropriate set.
     * 
     * @param info
     *            the information to add
     */
    void addScript(final BaseInfo info)
    {
        if (info instanceof TestScriptInfo)
        {
            tests.add((TestScriptInfo) info);
        }
        else if (info instanceof ModuleScriptInfo)
        {
            modules.add((ModuleScriptInfo) info);
        }
        else if (info instanceof JavaModuleInfo)
        {
            javaModules.add((JavaModuleInfo) info);
        }
        else
        {
            throw new IllegalArgumentException("Don't know how to handle class '" + info.getClass().getCanonicalName() + "'");
        }

        final String packageName = ScriptingUtils.getScriptPackage(info.getName());
        final PackageInfo pkgInfo = packages.get(packageName);
        if (pkgInfo == null)
        {
            throw new IllegalArgumentException("Don't know such package '" + packageName + "'");
        }
        pkgInfo.addScript(info);
    }

    /**
     * Adds the given package information to the internal set.
     * 
     * @param pkg
     *            the package information
     */
    void addPackage(final PackageInfo pkg)
    {
        packages.put(pkg.getName(), pkg);
    }

    /**
     * Returns the global test data.
     * 
     * @return global test data of this suite
     */
    public Map<String, String> getGlobalTestData()
    {
        return globalTestData;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the tests
     */
    public Collection<TestScriptInfo> getTests()
    {
        return tests;
    }

    /**
     * @return the modules
     */
    public Collection<ModuleScriptInfo> getModules()
    {
        return modules;
    }

    /**
     * @return the javaModules
     */
    public Collection<JavaModuleInfo> getJavaModules()
    {
        return javaModules;
    }

    /**
     * @return the packages
     */
    public Map<String, PackageInfo> getPackages()
    {
        return Collections.unmodifiableMap(packages);
    }
}
