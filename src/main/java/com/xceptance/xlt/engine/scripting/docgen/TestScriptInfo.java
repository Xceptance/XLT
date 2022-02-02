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
package com.xceptance.xlt.engine.scripting.docgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Test-case script information.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("testcase")
public class TestScriptInfo extends ScriptInfo
{
    String baseUrl;

    @XStreamAsAttribute
    boolean disabled;

    private final List<Step> postSteps = new ArrayList<>();

    transient int afterIndex;
    
    /**
     * @param description
     * @param tags
     */
    TestScriptInfo(final String name)
    {
        super(name);
    }

    TestScriptInfo(final String name, final String id)
    {
        super(name, id);
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public List<Step> getPostSteps()
    {
        return Collections.unmodifiableList(postSteps);
    }

    void addPostStep(final Step step)
    {
        if (step != null)
        {
            postSteps.add(step);
        }
    }

}
