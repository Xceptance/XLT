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
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlText;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlTextExclude;

/**
 * Url rules including text lookup {@link MergeRule}.
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Url_Test extends MergeRuleTestBase
{
    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match 
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("originalName");
            data2.setUrl("Any Transaction");
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }

    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlExcludePattern("http://foo.bar/.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match 
        RequestData data1 = new RequestData("originalName");
        data1.setUrl("http://foo.bar/123");
        assertEquals(1, rule.process(data1));
        assertEquals("originalName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        data2.setUrl("Transaction123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlExcludePattern("Fo+bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // both match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("https://nay.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("originalName");
            data.setUrl("https://any.hist/Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u}"),
                                new UrlPattern("http://foo.bar/(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setUrl("http://foo.bar/123");
        assertEquals(1, rule.process(data));
        assertEquals("URL:http://foo.bar/123", data.getName());
    }

    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setUrl("http://foo.bar/123");
        assertEquals(1, rule.process(data));
        assertEquals("URL:http://foo.bar/123", data.getName());
    }
    
    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u:0}"),
                                new UrlPattern("http://foo.bar/(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setUrl("http://foo.bar/123");
        assertEquals(1, rule.process(data));
        assertEquals("URL:http://foo.bar/123", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{u:1}:{u:2}:{u:1}"),
                                new UrlPattern("http://foo\\.(bar)/(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setUrl("http://foo.bar/123");
        assertEquals(1, rule.process(data));
        assertEquals("bar:123:bar", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Transaction"),
                                new UrlPattern("TransactionX.*"),
                                new StopOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setUrl("TransactionX42");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Transaction", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u}"),
                                new UrlPattern("TransactionY.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setUrl("TransactionY99");
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u}"),
                                new UrlPattern("TAddToCart.*"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setUrl("TAddToCart42");
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u:1}"),
                                new UrlPattern("TAddToCart(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setUrl("TAddToCart42");
        assertEquals(99, rule.process(data));
        assertEquals("URL:42", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("URL:{u:1}"),
                                new UrlPattern("TAddToCart(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setUrl("RocketZ42");
        assertEquals(90, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    // ============================================================================
    // Text
    // ============================================================================

    // text include
    @Test
    public void textInclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("foo"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setUrl("http://bar.com/foo/123");
        assertEquals(1, rule.process(data));
        assertEquals("newName", data.getName());

        // no match
        var data2 = new RequestData("originalName");
        data2.setUrl("http://bar.com/bar/123");
        assertEquals(1, rule.process(data2));
        assertEquals("originalName", data2.getName());
    }

    // text exclude
    @Test
    public void textExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlTextExclude("foo"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match (should be excluded)
        var data = new RequestData("originalName");
        data.setUrl("http://bar.com/foo/123");
        assertEquals(1, rule.process(data));
        assertEquals("originalName", data.getName());

        // no match (should be included)
        var data2 = new RequestData("originalName");
        data2.setUrl("http://bar.com/bar/123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    // text include + exclude
    @Test
    public void textIncludeExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("foo"),
                                new UrlTextExclude("bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins
        var data = new RequestData("originalName");
        data.setUrl("http://bar.com/foo/");
        assertEquals(1, rule.process(data));
        assertEquals("originalName", data.getName());

        // include only
        var data2 = new RequestData("originalName");
        data2.setUrl("http://any.com/foo/123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());

        // exclude only
        var data3 = new RequestData("originalName");
        data3.setUrl("http://bar.com/bar/123");
        assertEquals(1, rule.process(data3));
        assertEquals("originalName", data3.getName());

        // neither
        var data4 = new RequestData("originalName");
        data4.setUrl("http://majority.org/");
        assertEquals(1, rule.process(data4));
        assertEquals("originalName", data4.getName());
    }

    // url include, text include
    @Test
    public void urlInclude_textInclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlText("123"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        // text and pattern will be united into one!
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // both match 
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // url match only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text match only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url include, text exclude
    @Test
    public void urlInclude_textExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlTextExclude("123"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match 
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // url match only, but excluded
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text match only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url include, text include + exclude
    @Test
    public void urlInclude_textIncludeExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlText("text"),
                                new UrlTextExclude("exclude"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // all match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text/exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // include match, exclude miss
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text/nope");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        
        // pattern only, no enough
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text only, no enough
        {   
            var data = new RequestData("originalName");
            data.setUrl("http://text");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text exclude only, no change
        {
            var data = new RequestData("originalName");
            data.setUrl("http://exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/asdfasf");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url exclude, text include
    @Test
    public void urlExclude_textInclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("text"),
                                new UrlExcludePattern("http://foo.bar/.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins 
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // url exclude only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text match only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/text");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url exclude, text exclude
    @Test
    public void urlExclude_textExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlExcludePattern("http://foo.bar/.*"),
                                new UrlTextExclude("text"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match 
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // url exclude only
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/xyz");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // text exclude only, total not match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://baz.bar/text");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://asd.bar/123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
    }

    // url exclude, text include + exclude
    @Test
    public void urlExclude_textIncludeExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("text"),
                                new UrlTextExclude("exclude"),
                                new UrlExcludePattern("http://foo.bar/.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // all matches, excludes wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text-exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text yes, text exclude no, url exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://asdf.d/text-nope");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // text no, text exclude yes, url exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://asdf.d/exclude-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        
        // text no, text exclude yes, url exclude yes
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/exclude-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // text no, text exclude no, url exclude yes
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/sd-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }
    
    // url include + exclude, text include
    @Test
    public void urlIncludeExclude_textInclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("text"),
                                new UrlPattern("http://foo.bar/123/.*"),
                                new UrlExcludePattern("e?clude"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // text yes, pattern yes, exclude yes
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/text/exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text yes, pattern no, exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://sd.bar/123/text");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text yes, pattern yes, exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/text");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        // text yes, pattern no, exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://text.bar/123/s");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text no, pattern no, exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/asd/s");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text no, pattern yes, exclude no
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/s");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text no, pattern yes, exclude yes
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // text no, pattern no, exclude yes
        {
            var data = new RequestData("originalName");
            data.setUrl("http://asdf.bar/exclude/s");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url include + exclude, text exclude
    @Test
    public void urlIncludeExclude_textExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlTextExclude("text"),
                                new UrlExcludePattern("e[a-z]clude"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // pattern yes, text exclude yes, pattern exclude yes
        // text exclude wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/text-exclude");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

        // pattern yes, text exclude yes, pattern exclude no
        // text exclude wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/text-nope");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // pattern yes, text exclude no, pattern exclude yes
        // exclude is a miss, because it is and AND, so when
        // text exclude is a miss, we won't check the pattern exclude
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/none-ezclude");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // pattern yes, text exclude no, pattern exclude no
        // simplest version
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/none-sdfe");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        
        // pattern no, text exclude no, pattern exclude no
        // nothing fits
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.org/123/none-sdfe");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    // url include + exclude, text include + exclude
    @Test
    public void urlIncludeExclude_textIncludeExclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new UrlText("123"),
                                new UrlPattern("http://foo.bar/.*"),
                                new UrlTextExclude("xyz"),
                                new UrlExcludePattern("xy.?99(.*)"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // yes, yes, yes, yes -> text exclude wins
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/xyz99abc");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // yes, yes, yes, no -> both excludes must match
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/xyz-nope");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        // yes, yes, no, yes -> exclude text is miss, so
        // exclude does not win, text must win first aka
        // AND condition
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/nope-xyy99abc");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        // yes, yes, no, no -> include wins
        {  
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/123/nope-nope");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }
        // no, yes, no, no -> text include miss
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.bar/787/nope-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // yes, no, no, no -> pattern include miss
        {
            var data = new RequestData("originalName");
            data.setUrl("http://fxx.bar/nope-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no, no, no, no -> include miss
        {
            var data = new RequestData("originalName");
            data.setUrl("http://foo.org/876/nope-nope");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }
}
