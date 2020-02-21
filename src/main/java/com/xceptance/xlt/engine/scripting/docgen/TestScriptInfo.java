/*
 * File: TestScriptInfo.java
 * Created on: Nov 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
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
