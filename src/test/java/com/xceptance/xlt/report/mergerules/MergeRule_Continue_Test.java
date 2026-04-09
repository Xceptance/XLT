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
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Tests the @link MergeRule} continue on match and continue on no match functionality.
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Continue_Test extends MergeRuleTestBase
{
    // =================================================================================
    // ContinueOnMatch and ContinueOnNoMatch rule tests
    // =================================================================================

    @Test
    public void continueOnMatch() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false),
                                      new ContinueOnMatchAtId(99), 
                                      new ContinueOnNoMatchAtId(42));         

        final var data = new RequestData("request");
        assertEquals(99, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void continueOnMatch_ContinueNotSet() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false), 
                                      new ContinueOnMatchAtId(42), 
                                      new ContinueOnNoMatchAtId(42)); 

        final var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false), 
                                      new ContinueOnMatchAtId(42), 
                                      new ContinueOnNoMatchAtId(77)); 

        final var data = new RequestData("not-matching");
        assertEquals(77, rule.process(data));
        assertEquals("not-matching", data.getName());
    }

    @Test
    public void continueOnNoMatch_ContinueNotSet() throws Exception
    {
        final var rule = getMergeRule(42, 
                                      new NewName("fooBar"),
                                      new NamePattern("request"), 
                                      new StopOnMatch(false), 
                                      new ContinueOnMatchAtId(42), 
                                      new ContinueOnNoMatchAtId(42)); 

        final var data = new RequestData("not-matching");
        assertEquals(42, rule.process(data));
        assertEquals("not-matching", data.getName());
    }
}
