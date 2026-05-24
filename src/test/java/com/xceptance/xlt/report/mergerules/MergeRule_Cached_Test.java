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
import com.xceptance.xlt.report.mergerules.MergeRule.CachedExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.CachedPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Cached Request Pattern Tests
 * <p>Created by AI (Gemini 2.5 Pro).</p>
 */
public class MergeRule_Cached_Test extends MergeRuleTestBase
{
    @Test
    public void include_CachedIsTrue() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("CachedReq"),
                                new CachedPattern("true"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match (cached request)
        {
            var data = new RequestData("originalName");
            data.setCached(true);
            assertEquals(1, rule.process(data));
            assertEquals("CachedReq", data.getName());
        }

        // no match (non-cached request)
        {
            RequestData data2 = new RequestData("originalName");
            data2.setCached(false);
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }
    
    @Test
    public void exclude_CachedIsFalse() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new CachedExcludePattern("false"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match (cached request - so it does NOT match the exclude of "false")
        RequestData data1 = new RequestData("originalName");
        data1.setCached(true);
        assertEquals(1, rule.process(data1));
        assertEquals("NewName", data1.getName());

        // no match (non-cached request - matches the exclude "false", so it is skipped)
        RequestData data2 = new RequestData("originalName");
        data2.setCached(false);
        assertEquals(1, rule.process(data2));
        assertEquals("originalName", data2.getName());
    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("IsCached:{cache}"),
                                new CachedPattern(".*"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match true
        var data1 = new RequestData("originalName");
        data1.setCached(true);
        assertEquals(1, rule.process(data1));
        assertEquals("IsCached:true", data1.getName());

        // match false
        var data2 = new RequestData("originalName");
        data2.setCached(false);
        assertEquals(1, rule.process(data2));
        assertEquals("IsCached:false", data2.getName());
    }
}
