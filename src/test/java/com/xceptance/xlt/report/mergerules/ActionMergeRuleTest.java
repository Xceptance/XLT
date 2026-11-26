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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Tests for Action grouping/merging rules using {@link ActionDataRequestWrapper} and {@link MergeRuleProcessor}.
 */
public class ActionMergeRuleTest extends MergeRuleTestBase
{
    /**
     * Test that ActionDataRequestWrapper sets up safe defaults and delegates getters correctly.
     */
    @Test
    public void testWrapperDelegationAndDefaults()
    {
        final ActionData action = new ActionData("MyAction");
        action.setTime(123456789L);
        action.setRunTime(500);
        action.setFailed(true);

        final ActionDataRequestWrapper wrapper = new ActionDataRequestWrapper(action);

        // Delegated fields
        assertEquals("MyAction", wrapper.getName());
        assertEquals(123456789L, wrapper.getTime());
        assertEquals(500, wrapper.getRunTime());
        assertTrue(wrapper.hasFailed());

        // Safe defaults to prevent NPEs in Condition evaluation
        assertEquals("", wrapper.getUrl().toString());
        assertEquals("", wrapper.getContentType().toString());
        assertEquals("", wrapper.getHttpMethod().toString());
        assertEquals(200, wrapper.getResponseCode());

        // Setter delegation
        wrapper.setName("NewActionName");
        assertEquals("NewActionName", action.getName());
        assertEquals("NewActionName", wrapper.getName());
    }

    /**
     * Test applying a simple renaming merge rule on actions.
     */
    @Test
    public void testSimpleActionRenameRule() throws Exception
    {
        final var rule = getMergeRule(1,
                                      new NewName("GroupedAction"),
                                      new NamePattern("Search_.*"),
                                      new StopOnMatch(true),
                                      new DropOnMatch(false));

        final MergeRuleProcessor rp = new MergeRuleProcessor(List.of(rule), false);

        final ActionData action = new ActionData("Search_Query_123");
        action.setRunTime(300);
        action.setFailed(false);

        final ActionDataRequestWrapper wrapper = new ActionDataRequestWrapper(action);
        final var result = rp.postprocess(wrapper);

        assertNotNull(result);
        assertTrue(result instanceof ActionDataRequestWrapper);

        final ActionData processedAction = ((ActionDataRequestWrapper) result).getActionData();
        assertEquals("GroupedAction", processedAction.getName());
        assertEquals(300, processedAction.getRunTime());
        assertFalse(processedAction.hasFailed());
    }

    /**
     * Test applying a drop rule on actions.
     */
    @Test
    public void testActionDropRule() throws Exception
    {
        final var rule = getMergeRule(1,
                                      new NamePattern("Internal_.*"),
                                      new StopOnMatch(true),
                                      new DropOnMatch(true));

        final MergeRuleProcessor rp = new MergeRuleProcessor(List.of(rule), false);

        final ActionData action = new ActionData("Internal_Ping");
        final ActionDataRequestWrapper wrapper = new ActionDataRequestWrapper(action);

        final var result = rp.postprocess(wrapper);
        assertNull(result);
    }
}
