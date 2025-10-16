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
package com.xceptance.xlt.api.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.TestWrapper;

/**
 * Test the implementation of {@link BasicPageUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class BasicPageUtilsTest
{
    /**
     * Build an instance. Here to secure our API, because we left it open.
     */
    @Test
    public void constructor()
    {
        @SuppressWarnings("unused")
        final BasicPageUtils b = new BasicPageUtils();
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#pickOneRandomly(List, boolean, boolean)} by passing a null
     * reference as input list.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPickOneRandomly_NullList()
    {
        BasicPageUtils.pickOneRandomly(null, false, false);
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#pickOneRandomly(List, boolean, boolean)} by passing a
     * under-sized list as argument.
     */
    @Test
    public void testPickOneRandomly_ListTooSmall()
    {
        final List<Integer> testList = new ArrayList<Integer>();

        createAndExecuteWrapper(testList, false, false);

        testList.add(1);

        createAndExecuteWrapper(testList, true, false);
        createAndExecuteWrapper(testList, false, true);
        createAndExecuteWrapper(testList, true, true);
    }

    private <T> void createAndExecuteWrapper(final List<T> testList, final boolean excludeFirst, final boolean excludeLast)
    {
        new TestWrapper(AssertionError.class, null,
                        "BasicPageUtils#pickOneRandomly should raise an AssertionError since passed list argument is to small!")
        {
            @Override
            protected void run()
            {
                BasicPageUtils.pickOneRandomly(testList, excludeFirst, excludeLast);
            }
        }.execute();
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#pickOneRandomly(List, boolean, boolean)}.
     */
    @Test
    public void testPickOneRandomly()
    {
        final List<Integer> testList = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++)
        {
            testList.add(i);
        }

        final Set<Integer> returnedValues = new HashSet<Integer>();

        // test this more than once, because there are random items
        for (int j = 0; j < 100000; j++)
        {
            Integer i = BasicPageUtils.pickOneRandomly(testList);
            Assert.assertNotNull(i);
            returnedValues.add(i);
            Assert.assertEquals(0, Math.min(0, i.intValue()));
            Assert.assertEquals(9, Math.max(9, i.intValue()));

            i = BasicPageUtils.pickOneRandomly(testList, true);
            Assert.assertNotNull(i);
            returnedValues.add(i);
            Assert.assertEquals(1, Math.min(1, i.intValue()));
            Assert.assertEquals(9, Math.max(9, i.intValue()));

            i = BasicPageUtils.pickOneRandomly(testList, false, true);
            Assert.assertNotNull(i);
            returnedValues.add(i);
            Assert.assertEquals(0, Math.min(0, i.intValue()));
            Assert.assertEquals(8, Math.max(8, i.intValue()));

            i = BasicPageUtils.pickOneRandomly(testList, true, true);
            Assert.assertNotNull(i);
            returnedValues.add(i);
            Assert.assertEquals(1, Math.min(1, i.intValue()));
            Assert.assertEquals(8, Math.max(8, i.intValue()));
        }
        // all elements of the test list should be picked at least one time
        Assert.assertEquals(10, returnedValues.size());
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#getAbsoluteUrl(String, String)} by passing an invalid base URL.
     * 
     * @throws MalformedURLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetAbsoluteUrl_BaseUrlInvalid_Null() throws MalformedURLException
    {
        BasicPageUtils.getAbsoluteUrl((String) null, "Any String");
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#getAbsoluteUrl(String, String)} by passing an invalid base URL.
     * 
     * @throws MalformedURLException
     */
    @Test(expected = MalformedURLException.class)
    public void testGetAbsoluteUrl_BaseUrlInvalid() throws MalformedURLException
    {
        BasicPageUtils.getAbsoluteUrl("InvalidURL", "Any String");
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#getAbsoluteUrl(String, String)} by passing an invalid relative
     * URL.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetAbsoluteUrl_RelativeUrlInvalid() throws MalformedURLException
    {
        BasicPageUtils.getAbsoluteUrl("http://localhost", null);
    }

    /**
     * Tests the implementation of {@link BasicPageUtils#getAbsoluteUrl(String, String)}.
     */
    @Test
    public void testGetAbsoluteUrl() throws MalformedURLException
    {
        Assert.assertEquals("http://localhost/example.html", BasicPageUtils.getAbsoluteUrl("http://localhost", "example.html"));
    }
}
