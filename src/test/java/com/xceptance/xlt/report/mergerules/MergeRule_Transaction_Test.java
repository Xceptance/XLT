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
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNamePattern;

/**
 * Transaction Type Pattern Tests
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Transaction_Test extends MergeRuleTestBase
{
    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new TransactionNamePattern("MyTransaction.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match 
        {
            var data = new RequestData("originalName");
            data.setTransactionName("MyTransaction123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("originalName");
            data2.setTransactionName("Any Transaction");
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }
    
    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new TransactionNameExcludePattern("MyTransaction.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // match 
        RequestData data1 = new RequestData("originalName");
        data1.setTransactionName("MyTransaction123");
        assertEquals(1, rule.process(data1));
        assertEquals("originalName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        data2.setTransactionName("Transaction123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new TransactionNamePattern("MyTransaction.*"),
                                new TransactionNameExcludePattern("Fo+bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // both match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setTransactionName("MyTransactionFoobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            data.setTransactionName("AnythingElse");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("originalName");
            data.setTransactionName("MyTransaction123");
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("originalName");
            data.setTransactionName("Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t}"),
                                new TransactionNamePattern("MyTransaction(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setTransactionName("MyTransaction123");
        assertEquals(1, rule.process(data));
        assertEquals("Transaction:MyTransaction123", data.getName());
    }
    
    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setTransactionName("MyTransaction123");
        assertEquals(1, rule.process(data));
        assertEquals("Transaction:MyTransaction123", data.getName());
    }
    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t:0}"),
                                new TransactionNamePattern("MyTransaction(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setTransactionName("MyTransaction123");
        assertEquals(1, rule.process(data));
        assertEquals("Transaction:MyTransaction123", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{t:1}:{t:2}:{t:1}"),
                                new TransactionNamePattern("My(Transaction)(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setTransactionName("MyTransaction123");
        assertEquals(1, rule.process(data));
        assertEquals("Transaction:123:Transaction", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction"),
                                new TransactionNamePattern("TransactionX.*"),
                                new StopOnMatch(true)
            );

        var data = new RequestData("originalName");
        data.setTransactionName("TransactionX42");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Transaction", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t}"),
                                new TransactionNamePattern("TransactionY.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setTransactionName("TransactionY99");
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t}"),
                                new TransactionNamePattern("TAddToCart.*"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setTransactionName("TAddToCart42");
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }
    
    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t:1}"),
                                new TransactionNamePattern("TAddToCart(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setTransactionName("TAddToCart42");
        assertEquals(99, rule.process(data));
        assertEquals("Transaction:42", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction:{t:1}"),
                                new TransactionNamePattern("TAddToCart(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setTransactionName("RocketZ42");
        assertEquals(90, rule.process(data));
        assertEquals("originalName", data.getName());
    }
}
