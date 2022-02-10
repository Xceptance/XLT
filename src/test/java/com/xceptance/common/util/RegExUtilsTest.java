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
package com.xceptance.common.util;

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.common.lang.ReflectionUtils;

/**
 * @author rschwietzke
 */
public class RegExUtilsTest
{
    /**
     * Clean up our mess
     */
    @Before
    @After
    public void resetInternalStorage()
    {
        final ConcurrentLRUCache<?, Pattern> patternCache = ReflectionUtils.readField(RegExUtils.class, null, "patternCache");
        final ConcurrentLRUCache<String, Pattern> resultCache = ReflectionUtils.readField(RegExUtils.class, null, "resultCache");

        patternCache.clear();
        resultCache.clear();
    }

    /**
     * Assert internal data size to see if caching works
     * 
     * @param patternCacheSize
     * @param resultCacheSize
     */
    private final void assertInternalStructure(final int patternCacheSize, final int resultCacheSize)
    {
        final ConcurrentLRUCache<?, Pattern> patternCache = ReflectionUtils.readField(RegExUtils.class, null, "patternCache");
        final ConcurrentLRUCache<String, Pattern> resultCache = ReflectionUtils.readField(RegExUtils.class, null, "resultCache");

        Assert.assertEquals(patternCacheSize, patternCache.size());
        Assert.assertEquals(resultCacheSize, resultCache.size());
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getMatchingCount(java.lang.String, java.util.regex.Pattern)}.
     */
    @Test
    public final void testGetMatchingCount_StringPattern()
    {
        Assert.assertEquals(1, RegExUtils.getMatchingCount("abc", Pattern.compile("a")));
        assertInternalStructure(0, 0); // no caching for delivered patterns

        Assert.assertEquals(1, RegExUtils.getMatchingCount("abc", Pattern.compile("abc")));
        assertInternalStructure(0, 0); // no caching for delivered patterns

        Assert.assertEquals(2, RegExUtils.getMatchingCount("abcabc", Pattern.compile("abc")));
        assertInternalStructure(0, 0); // no caching for delivered patterns
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#getMatchingCount(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void testGetMatchingCount_StringString()
    {
        Assert.assertEquals(1, RegExUtils.getMatchingCount("abc", "a"));
        assertInternalStructure(1, 0);

        Assert.assertEquals(1, RegExUtils.getMatchingCount("abc", "abc"));
        assertInternalStructure(2, 0);

        Assert.assertEquals(2, RegExUtils.getMatchingCount("abcabc", "abc"));
        assertInternalStructure(2, 0);
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#getPattern(java.lang.String)}.
     */
    @Test
    public final void testGetPatternString()
    {
        final Pattern pattern1 = RegExUtils.getPattern("[abc]+");
        Assert.assertEquals("[abc]+", pattern1.pattern());
        assertInternalStructure(1, 0);

        final Pattern pattern2 = RegExUtils.getPattern("[abc]+");
        Assert.assertEquals("[abc]+", pattern2.pattern());
        assertInternalStructure(1, 0);

        Assert.assertTrue(pattern1 == pattern2);
    }

    /**
     * DOTALL is on
     */
    @Test
    public final void testGetPatternString_DOTALL_is_On()
    {
        // fails when not DOTALL
        final Pattern pattern1 = RegExUtils.getPattern("^.+$");
        Assert.assertTrue(RegExUtils.isMatching("a\na", pattern1));
        Assert.assertEquals(1, RegExUtils.getMatchingCount("a\na", pattern1));
    }

    @Test
    public final void testGetPatternString_DOTALL_is_Off_Test()
    {
        // prove of claim before, only MULTILINE, not DOTALL
        final Pattern pattern1 = RegExUtils.getPattern("^a.+a$", Pattern.MULTILINE);
        Assert.assertFalse(RegExUtils.isMatching("a\na", pattern1));
    }

    @Test
    public final void testGetPatternString_MULTILINE_is_On()
    {
        // fails when not MULTILINE
        final Pattern pattern2 = RegExUtils.getPattern("^a+$");
        Assert.assertEquals(2, RegExUtils.getMatchingCount("a\na", pattern2));
    }

    @Test
    public final void testGetPatternString_MULTILINE_is_Off_Test()
    {
        // prove of claim before, only DOTALL, NOT MULTILINE
        final Pattern pattern2 = RegExUtils.getPattern("^a+$", Pattern.DOTALL);
        Assert.assertFalse(RegExUtils.isMatching("a\na", pattern2));
        Assert.assertEquals(0, RegExUtils.getMatchingCount("a\na", pattern2));
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#getPattern(java.lang.String, int)}.
     */
    @Test
    public final void testGetPatternStringInt_CachingCheck()
    {
        final Pattern pattern1 = RegExUtils.getPattern("^a+$", Pattern.MULTILINE);
        Assert.assertEquals(2, RegExUtils.getMatchingCount("a\na", pattern1));
        assertInternalStructure(1, 0);

        final Pattern pattern2 = RegExUtils.getPattern("^a+$", Pattern.DOTALL);
        Assert.assertEquals(0, RegExUtils.getMatchingCount("a\na", pattern2));
        assertInternalStructure(2, 0);

        Assert.assertTrue(pattern1 != pattern2);
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#isMatching(java.lang.String, java.util.regex.Pattern)}.
     */
    @Test
    public final void testIsMatchingStringPattern()
    {
        Assert.assertTrue(RegExUtils.isMatching("foobar", Pattern.compile("foo")));
        Assert.assertFalse(RegExUtils.isMatching("fuubar", Pattern.compile("foo")));
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#isMatching(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testIsMatchingStringString()
    {
        Assert.assertTrue(RegExUtils.isMatching("foobar", "foo"));
        assertInternalStructure(1, 0);

        Assert.assertFalse(RegExUtils.isMatching("fuubar", "foo"));
        assertInternalStructure(1, 0);
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getAllMatches(java.lang.String, java.util.regex.Pattern)}.
     */
    @Test
    public final void testGetAllMatchesStringPattern()
    {
        Assert.assertArrayEquals(new String[]
            {
                "foo", "fou", "foa"
            }, RegExUtils.getAllMatches("foo-fou-foa", Pattern.compile("fo[oua]")).toArray());

        Assert.assertArrayEquals(new String[] {}, RegExUtils.getAllMatches("foo-fou-foa", Pattern.compile("FO[oua]")).toArray());
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getAllMatches(java.lang.String, java.util.regex.Pattern, int)}.
     */
    @Test
    public final void testGetAllMatchesStringPatternInt()
    {
        Assert.assertArrayEquals(new String[]
            {
                "o", "u", "a"
            }, RegExUtils.getAllMatches("foo-fou-foa", Pattern.compile("fo([oua])"), 1).toArray());
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getAllMatches(java.lang.String, java.util.regex.Pattern, int)}.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public final void testGetAllMatchesStringPatternInt_Exception()
    {
        Assert.assertArrayEquals(new String[]
            {
                "foo", "fou", "foa"
            }, RegExUtils.getAllMatches("foo-fou-foa", "fo[oua]", 1).toArray());
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#getAllMatches(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testGetAllMatchesStringString()
    {
        Assert.assertArrayEquals(new String[]
            {
                "foo", "fou", "foa"
            }, RegExUtils.getAllMatches("foo-fou-foa", "fo[oua]").toArray());

        Assert.assertArrayEquals(new String[] {}, RegExUtils.getAllMatches("foo-fou-foa", "FO[oua]").toArray());
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getAllMatches(java.lang.String, java.lang.String, int)}.
     */
    @Test
    public final void testGetAllMatchesStringStringInt()
    {
        Assert.assertArrayEquals(new String[]
            {
                "o", "u", "a"
            }, RegExUtils.getAllMatches("foo-fou-foa", "fo([oua])", 1).toArray());
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getFirstMatch(java.lang.String, java.util.regex.Pattern)}.
     */
    @Test
    public final void testGetFirstMatchStringPattern()
    {
        Assert.assertEquals("aBc", RegExUtils.getFirstMatch("aBcaFc", Pattern.compile("a[A-Z]c")));
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getFirstMatch(java.lang.String, java.util.regex.Pattern, int)}.
     */
    @Test
    public final void testGetFirstMatchStringPatternInt()
    {
        Assert.assertEquals("B", RegExUtils.getFirstMatch("aBcaFc", Pattern.compile("a([A-Z])c"), 1));
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#getFirstMatch(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testGetFirstMatchStringString()
    {
        Assert.assertEquals("aBc", RegExUtils.getFirstMatch("aBcaFc", "a[A-Z]c"));
        assertInternalStructure(1, 0);
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#getFirstMatch(java.lang.String, java.lang.String, int)}.
     */
    @Test
    public final void testGetFirstMatchStringStringInt()
    {
        Assert.assertEquals("B", RegExUtils.getFirstMatch("aBcaFc", "a([A-Z])c", 1));
        assertInternalStructure(1, 0);
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.RegExUtils#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testReplaceAll()
    {
        Assert.assertEquals("AAAf", RegExUtils.replaceAll("bcaf", "[abc]", "A"));
        Assert.assertEquals("1241", RegExUtils.replaceAll("1241", "[abc]", "A"));
    }

    /**
     * Test method for {@link com.xceptance.common.util.RegExUtils#escape(java.lang.String)}.
     */
    @Test
    public final void testEscape()
    {
        final String q1 = RegExUtils.escape(".?[a]");
        Assert.assertEquals("\\Q.?[a]\\E", q1);
        assertInternalStructure(0, 1);

        final String q2 = RegExUtils.escape(".?[a]");
        Assert.assertEquals("\\Q.?[a]\\E", q2);
        assertInternalStructure(0, 1);

        Assert.assertTrue(q1 == q2);
    }

    @Test
    public final void testEscape_Empty()
    {
        Assert.assertEquals(null, RegExUtils.escape(null));
        assertInternalStructure(0, 0);

        Assert.assertEquals("", RegExUtils.escape(""));
        assertInternalStructure(0, 0);
    }
    
    @Test
    public final void testRemoveAll_groupZero()
    {
        Assert.assertEquals("bar", RegExUtils.removeAll("foobar", "f(o)o", 0));
    }
    
    @Test
    public final void testRemoveAll_groupZeroImplicitly()
    {
        Assert.assertEquals("bar", RegExUtils.removeAll("foobar", "f(o)o"));
    }
    
    @Test
    public final void testRemoveAll_groupOne()
    {
        Assert.assertEquals("fobar", RegExUtils.removeAll("foobar", "f(o)o", 1));
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public final void testRemoveAll_groupTooHigh()
    {
        Assert.assertEquals("foobar", RegExUtils.removeAll("foobar", "f(o)o", 5));
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public final void testRemoveAll_groupNegative()
    {
        final String s = "foobar";
        Assert.assertEquals(s, RegExUtils.removeAll(s, "foo", -1));
    }
    
    @Test
    public final void testRemoveAll_noMatch()
    {
        final String s = "foobar";
        Assert.assertEquals(s, RegExUtils.removeAll(s, "baz"));
    }
    
    @Test
    public final void testRemoveAll_noMatch_groupNegative()
    {
        final String s = "foobar";
        Assert.assertEquals(s, RegExUtils.removeAll(s, "baz", -1));
    }
    
    @Test
    public final void testRemoveAll_manyMatches()
    {
        Assert.assertEquals("fo bar fo bar fo bar", RegExUtils.removeAll("foo bar foo bar foo bar", "f(o)o", 1));
    }
    
    @Test
    public final void testRemoveAll_manyMatchesSuccessive()
    {
        Assert.assertEquals("fofofobar", RegExUtils.removeAll("foofoofoobar", "f(o)o", 1));
    }
    
    @Test
    public final void testRemoveAll_matchAtEnd()
    {
        Assert.assertEquals("barfo", RegExUtils.removeAll("barfoo", "f(o)o", 1));
    }
    
    @Test
    public final void testRemoveAll_completeMatch()
    {
        Assert.assertEquals("fo", RegExUtils.removeAll("foo", "f(o)o", 1));
    }
    
    @Test
    public final void testRemoveAll_dontMatchRecursively()
    {
        Assert.assertEquals("fffooo", RegExUtils.removeAll("fffoooo", "f(o)o", 1));
    }
    
    @Test
    public final void testRemoveAll_clearString()
    {
        Assert.assertEquals("", RegExUtils.removeAll("foo", ".*"));
    }
    
    @Test
    public final void testRemoveAll_textNull()
    {
        Assert.assertNull(RegExUtils.removeAll(null, "foo"));
    }
    
    @Test
    public final void testRemoveAll_regexNull()
    {
        final String s = "foobar";
        Assert.assertEquals(s, RegExUtils.removeAll(s, null));
    }
}
