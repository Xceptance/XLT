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
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlText;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlTextExclude;

/**
 * Tests the request renaming magic implemented by {@link MergeRule}.
 * This is for failing cases where the regexp is wrong or the logical 
 * setup in terms of jumps, also when the matching group is not there. 
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Errors_Test extends MergeRuleTestBase
{
    @Test
    public void invalidRegex() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> 
        {
            getMergeRule(0, 
                         new NewName("{n:1}"),
                         new NamePattern("([]-]"),
                         new StopOnMatch(true), 
                         new ContinueOnMatchAtId(0), 
                         new ContinueOnNoMatchAtId(0),         
                         new DropOnMatch(false),
                         new UrlText(""),
                         new UrlTextExclude("")); 
        }); 
        assertEquals("Invalid regular expression: ([]-]", ex.getMessage());
    }

    @Test
    public void validate_emptyFilterPatterns_invalidCapturingGroupIndex_1() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> 
        {
            // cannot refer to the group 1 in an empty pattern
            getMergeRule(1, 
                         new NewName("{n:1}"),
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(1), 
                         new ContinueOnNoMatchAtId(2),         
                         new DropOnMatch(false),
                         new UrlText(""));
        }); 
        assertEquals("Pattern '' has no matching group '1'. Important: You can only capture in include rules.", ex.getMessage());
    }


    @Test
    public void validate_nonEmptyFilterPatterns_invalidCapturingGroupIndex() throws Exception
    {
        // cannot refer to the group 2 in a pattern with just one group
        var ex = assertThrows(InvalidMergeRuleException.class, () -> 
        {
            getMergeRule(1, 
                         new NewName("{n:2}"),
                         new NamePattern("foo(bar)"),
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(1), 
                         new ContinueOnNoMatchAtId(2),         
                         new DropOnMatch(false));      
        }); 
        assertEquals("Pattern 'foo(bar)' has no matching group '2'. Important: You can only capture in include rules.", ex.getMessage());
    }


    @Test
    public void invalidContinueOnMatch() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> 
        {
            getMergeRule(42, 
                         new NewName("fooBar"),
                         new NamePattern("request"), 
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(9), 
                         new ContinueOnNoMatchAtId(42),         
                         new DropOnMatch(false));
        }); 
        assertEquals("Continue on match rule ID (9) must be greater or same than the rule ID (42)", ex.getMessage());
    }

    @Test
    public void invalidContinueOnNoMatch() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> 
        {
            getMergeRule(42, 
                         new NewName("fooBar"),
                         new NamePattern("request"), 
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(42), 
                         new ContinueOnNoMatchAtId(4),         
                         new DropOnMatch(false));
        }); 
        assertEquals("Continue on no match rule ID (4) must be greater or same than the rule ID (42)", ex.getMessage());
    }

    /**
     * Ensure that we see that the group index is not parseable
     */
    @Test
    public void cannotParseGroupIndex() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> {
            getMergeRule(42, 
                         new NewName("{n:a}"),
                         new NamePattern("request"), 
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(42), 
                         new ContinueOnNoMatchAtId(42),         
                         new DropOnMatch(false));
        }); 
        assertEquals("Failed to parse the matching group index 'a' as integer", ex.getMessage());
    }

    /**
     * We cannot have capturing with only exclude rules
     */
    @Test
    public void cannotHaveCapturingInNamePatternWithOnlyExcludes() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> {
            getMergeRule(1, 
                         new NewName("{n:1}"),
                         new NameExcludePattern("(foo)"),
                         new StopOnMatch(false), 
                         new ContinueOnMatchAtId(1), 
                         new ContinueOnNoMatchAtId(1),         
                         new DropOnMatch(false));
        }); 
        assertEquals("Pattern '' has no matching group '1'. Important: You can only capture in include rules.", ex.getMessage());
    }

}
