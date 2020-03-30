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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Information common to all scripts (tests and modules).
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class ScriptInfo extends BaseInfo
{
    // @XStreamConverter(value=MapConverter.class)
    @XStreamAlias("external-parameters")
    private final Map<String, String> externalParameters = new HashMap<String, String>();

    private final List<Step> steps = new ArrayList<Step>();

    @XStreamOmitField
    private final HashSet<String> stores = new HashSet<String>();

    private final HashSet<String> calls = new HashSet<String>();

    @XStreamAlias("test-data")
    private Map<String, String> testData;

    ScriptInfo(final String name)
    {
        super(name);
    }

    ScriptInfo(final String name, final String id)
    {
        super(name, id);
    }

    void addStep(final Step step)
    {
        steps.add(step);
    }

    void addCall(final String call)
    {
        calls.add(call);
    }

    void addExternalParam(final String name, final String value)
    {
        externalParameters.put(name, value);
    }

    void setTestData(final Map<String, String> testData)
    {
        this.testData = new HashMap<String, String>();
        if (testData != null)
        {
            this.testData.putAll(testData);
        }
    }

    /**
     * @return the testData
     */
    public Map<String, String> getTestData()
    {
        return testData;
    }

    /**
     * @param value
     */
    void addStore(String value)
    {
        stores.add(value);
    }

    boolean hasStore(final String value)
    {
        return stores.contains(value);
    }

    public Set<String> getCalls()
    {
        return Collections.unmodifiableSet(calls);
    }

    /**
     * @return the steps
     */
    public List<Step> getSteps()
    {
        return Collections.unmodifiableList(steps);
    }

    void removeFromExternals(final Set<String> keys)
    {
        for (final String key : keys)
        {
            externalParameters.remove(key);
        }
    }

    /**
     * @return the stores
     */
    public Set<String> getStores()
    {
        return Collections.unmodifiableSet(stores);
    }

    /**
     * @return the externalParameters
     */
    public Map<String, String> getExternalParameters()
    {
        return Collections.unmodifiableMap(externalParameters);
    }
}
