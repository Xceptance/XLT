/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Tests the request renaming magic implemented by {@link MergeRule}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Simpliest_Test extends MergeRuleTestBase
{
    // =================================================================================
    // Test Simplest Usage Patterns
    // =================================================================================
    @Test
    public void happyPath_Match() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false));         

        final var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void happyPath_Match_SimpleRegexContains() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false));         

        final var data = new RequestData("all request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void happyPath_Match_FullMatch() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("^request$"), 
                                      new StopOnMatch(false));         

        final var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void happyPath_NoMatch() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("rocket"), 
                                      new StopOnMatch(false));         

        final var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("request", data.getName());
    }
    
    @Test
    public void noRules() throws Exception
    {
        // no rules is possible
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new StopOnMatch(false));         

        final var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }
}
