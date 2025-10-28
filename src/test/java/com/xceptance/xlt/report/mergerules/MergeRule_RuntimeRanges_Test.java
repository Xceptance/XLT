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
package com.xceptance.xlt.report.mergerules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.RunTimeRanges;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Tests the request renaming magic implemented by {@link MergeRule}.
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_RuntimeRanges_Test extends MergeRuleTestBase
{
    // no range
    @Test
    public void noRange() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("test"),
                                      new RunTimeRanges(""),
                                      new StopOnMatch(false)
            ); 
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        final RequestData data = new RequestData("original");
        data.setRunTime(10);
        assertEquals(3, rule.process(data));
        assertEquals("test", data.getName());
    }

    // simple number range
    @Test
    public void oneNumberRangeNoName() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("test"),
                                      new RunTimeRanges("100"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("test", data.getName());
        }
        // hit
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("test", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(110);
            assertEquals(3, rule.process(data));
            assertEquals("test", data.getName());
        }
    }

    // simple number range
    @Test
    public void oneNumberRangeRename() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // hit
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=100", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(110);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=100", data.getName());
        }
    }
    // simple number range
    @Test
    public void oneNumberRangeRenameAndGroup0() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r:0}"),
                                      new RunTimeRanges("100"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // hit
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=100", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(110);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=100", data.getName());
        }
    }

    // simple number range, group is ignored
    @Test
    public void twoNumbers() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100,200"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=200", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(250);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=200", data.getName());
        }
    }

    // simple number range, group is ignored
    @Test
    public void threeNumbers() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100,200,3000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
    }

    // =================================================================================
    // Grammar
    // =================================================================================

    // space
    @Test
    public void spaces() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100 200 3000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
    }

    // ,
    @Test
    public void comma() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100,200,3000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
    }

    // semicoln
    @Test
    public void semicolon() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100;200;3000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
    }

    // formatted range with , and space 
    @Test
    public void commanAndSpace() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100, 200, 3000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=3000", data.getName());
        }
    }

    // mixed
    @Test
    public void mixed() throws Exception
    {
        final var rule = getMergeRule(3, 
                                      new NewName("Range:{r}"),
                                      new RunTimeRanges("100,200;3000  4000"),
                                      new StopOnMatch(false)
            ); 
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // under
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(10);
            assertEquals(3, rule.process(data));
            assertEquals("Range:0..99", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(100);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(150);
            assertEquals(3, rule.process(data));
            assertEquals("Range:100..199", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(200);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // between
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(300);
            assertEquals(3, rule.process(data));
            assertEquals("Range:200..2999", data.getName());
        }
        // on
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(3000);
            assertEquals(3, rule.process(data));
            assertEquals("Range:3000..3999", data.getName());
        }
        // over
        {
            final RequestData data = new RequestData("original");
            data.setRunTime(4001);
            assertEquals(3, rule.process(data));
            assertEquals("Range:>=4000", data.getName());
        }
    }

    // simple number range, group or other regex
    @Test(expected = InvalidMergeRuleException.class)
    public void regexInPattern() throws Exception
    {
        getMergeRule(3, 
                     new NewName("Range:{r:1}"),
                     new RunTimeRanges("10([0-9])"),
                     new StopOnMatch(false)
            ); 
    }

    // simple number range, group is not needed
    @Test(expected = InvalidMergeRuleException.class)
    public void oneNumberRangeRenameAndGroup1() throws Exception
    {
        getMergeRule(3, 
                     new NewName("Range:{r:1}"),
                     new RunTimeRanges("100"),
                     new StopOnMatch(false)
            ); 
    }
}
