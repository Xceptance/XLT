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
package com.xceptance.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests implementation of {@link StringMatcher}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class StringMatcherTest
{
    /**
     * Test instance of StringMatcher.
     */
    private StringMatcher matcher;

    /**
     * Test string accepting functionality of matcher.
     */
    @Test
    public void testIsAccepted()
    {
        // include all '*.java' files that do not contain the string 'Manager'
        matcher = new StringMatcher(".*\\.java", ".*Manager.*");
        Assert.assertFalse(matcher.isAccepted("TestAccountManager.java"));
        Assert.assertTrue(matcher.isAccepted("A.java"));

        // include all '*.java' and '*ManagerImpl*' files that do not contain
        // the string 'Manager'
        // NOTE: exclude pattern takes precedence
        matcher = new StringMatcher(".*\\.java .*ManagerImpl.*", ".*Manager.*");
        Assert.assertFalse(matcher.isAccepted("TestAccountManagerImpl.java"));

        // include all '*.java' files that do not contain the string
        // 'ManagerImpl'
        matcher = new StringMatcher(".*\\.java", ".*Manager[^I][^m][^p][^l].*");
        Assert.assertTrue(matcher.isAccepted("TestAccountManagerImpl.java"));

        // include all files that do not contain the string 'Manager'
        matcher = new StringMatcher(null, ".*Manager.*");
        Assert.assertFalse(matcher.isAccepted("TestAccountManagerImpl.java"));

        // same as before, using empty string instead of null
        matcher = new StringMatcher("", ".*Manager.*");
        Assert.assertFalse(matcher.isAccepted("TestAccountManagerImpl.java"));

        // include all '*.java' files
        matcher = new StringMatcher(".*\\.java", null);
        Assert.assertFalse(matcher.isAccepted("test"));
        Assert.assertTrue(matcher.isAccepted("ATest.java"));

        // same as before, using empty string instead of null
        matcher = new StringMatcher(".*\\.java", "");
        Assert.assertFalse(matcher.isAccepted("test"));
        Assert.assertTrue(matcher.isAccepted("ATest.java"));

        // include all files -> matches any string (even null)
        matcher = new StringMatcher(null, null);
        Assert.assertTrue(matcher.isAccepted("A.java"));
        Assert.assertTrue(matcher.isAccepted(""));
        Assert.assertTrue(matcher.isAccepted(null));
    }

    @Test
    public void testIsAccepted_include_fullMatch()
    {
        StringMatcher matcher = new StringMatcher("TOrder", null, true);

        Assert.assertTrue(matcher.isAccepted("TOrder"));
        Assert.assertFalse(matcher.isAccepted("TOrder_US"));
        Assert.assertFalse(matcher.isAccepted("Order"));
    }

    @Test
    public void testIsAccepted_exclude_fullMatch()
    {
        StringMatcher matcher = new StringMatcher(null, "TOrder", true);

        Assert.assertFalse(matcher.isAccepted("TOrder"));
        Assert.assertTrue(matcher.isAccepted("TOrder_US"));
        Assert.assertTrue(matcher.isAccepted("Order"));
    }
}
