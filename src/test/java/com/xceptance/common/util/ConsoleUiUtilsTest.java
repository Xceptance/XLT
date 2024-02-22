/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.common.util.ConsoleUiUtils.EofHandler;

/**
 * @author Sebastian Oerding
 */
public class ConsoleUiUtilsTest
{
    @Test
    public void testClassHasOnlyPrivateConstructors()
    {
        final String name = ConsoleUiUtils.class.getCanonicalName();
        Assert.assertTrue(name + " class should only have private constructors to prevent instantiation!",
                          ReflectionUtils.classHasOnlyPrivateConstructors(ConsoleUiUtils.class));
    }

    @Test
    public void testAddEofHandler()
    {
        final Set<EofHandler> eofHandlers = ReflectionUtils.readStaticField(ConsoleUiUtils.class, "EOF_HANDLERS");
        final int size = eofHandlers.size();
        final EofHandler eofHandler = new MyEofHandler();
        ConsoleUiUtils.addEofHandler(eofHandler);
        Assert.assertEquals("Wrong number of eof handlers, ", size + 1, eofHandlers.size());
        boolean matched = false;
        for (final EofHandler e : eofHandlers)
        {
            if (e == eofHandler)
            {
                matched = true;
                break;
            }
        }
        Assert.assertTrue("Expected my eof handler to be contained but it is not!", matched);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEofHandler2()
    {
        ConsoleUiUtils.addEofHandler(null);
    }

    @Test
    public void testConfirm()
    {
        /* The name of the static field to access. */
        final String fieldName = "STDIN_READER";
        /*
         * The input for the test, with n\nN\ny\nY representing the different choices which have to be separated by line
         * breaks.
         */
        final String testMessage = "Test this ";
        final byte[] input = "n\nN\ny\nY".getBytes();
        /* The new reader from which to consume data for the test. */
        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input)));
        /*
         * Manipulating the tested class to use our reader for the next calls. This may be a problem in case of multi
         * threaded execution of JUnit tests!
         */
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, br);
        /* Verify the expected results. */
        Assert.assertFalse("Input 'n' should give false!", ConsoleUiUtils.confirm(testMessage));
        Assert.assertFalse("Input 'N' should give false!", ConsoleUiUtils.confirm(testMessage));
        Assert.assertTrue("Input 'y' should give false!", ConsoleUiUtils.confirm(testMessage));
        Assert.assertTrue("Input 'Y' should give false!", ConsoleUiUtils.confirm(testMessage));
        /* Restore the original reader. Using the old object reference does not work. */
        final BufferedReader original = new BufferedReader(new InputStreamReader(System.in));
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, original);
    }

    @Test
    public void testReadInt()
    {
        /* The name of the static field to access. */
        final String fieldName = "STDIN_READER";
        /*
         * The input for the test, with 1\n13\n-3\n0\n5 representing the different choices which have to be separated by
         * line breaks.
         */
        final String testMessage = "Test this ";
        final byte[] input = "1\n13\n-3\n0\n5".getBytes();
        /* The new reader from which to consume data for the test. */
        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input)));
        /*
         * Manipulating the tested class to use our reader for the next calls. This may be a problem in case of multi
         * threaded execution of JUnit tests!
         */
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, br);
        /* Verify the expected results. */
        Assert.assertEquals("Input '1' should return 1!", 1, ConsoleUiUtils.readInt(testMessage));
        Assert.assertEquals("Input '13' should return 13!", 13, ConsoleUiUtils.readInt(testMessage));
        // Notice that values smaller than 1 are skipped!
        Assert.assertEquals("Input '-3 0 5' should return 5!", 5, ConsoleUiUtils.readInt(testMessage));
        /* Restore the original reader. Using the old object reference does not work. */
        final BufferedReader original = new BufferedReader(new InputStreamReader(System.in));
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, original);
    }

    /**
     * Verifies that the correct int values are read in.
     */
    @Test
    public void testMultiSelectItems()
    {
        /* The name of the static field to access. */
        final String fieldName = "STDIN_READER";
        final List<Integer> items = Arrays.asList(1, 2, 3, 4);
        final List<Integer> expected = Arrays.asList(1, 3);

        // check behaviour, adding only two specific items while true is given as flag
        Assert.assertEquals("Wrong indices in result, ", expected, doMultiselection("1;3", items, true));

        // check behaviour, input 0 and flag set to true
        Assert.assertEquals("Wrong indices in result, ", items, doMultiselection("0", items, true));

        // check behaviour, input 0 and flag set to false
        Assert.assertEquals("Wrong indices in result, ", Collections.emptyList(), doMultiselection("0", items, false));

        // check behaviour, concatenated usage of the separator chars
        Assert.assertEquals("Wrong indices in result, ", expected, doMultiselection("1,  ;3", items, false));

        // check behaviour, for each index an item is at most added once to the result
        Assert.assertEquals("Wrong indices in result, ", Arrays.asList(1), doMultiselection("1, ;1", items, false));

        /* Restore the original reader. Using the old object reference does not work. */
        final BufferedReader original = new BufferedReader(new InputStreamReader(System.in));
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, original);
    }

    /**
     * Verifies that the right object is returned, if the keys are not set.
     */
    @Test
    public void testSelectItemKeyIsNotSet()
    {
        /* The name of the static field to access. */
        final String fieldName = "STDIN_READER";
        /*
         * The input for the test, with 1\n2 representing the different choices which have to be separated by line
         * breaks.
         */
        final byte[] input = "1\n2".getBytes();
        /* The new reader from which to consume data for the test. */
        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input)));
        /*
         * Manipulating the tested class to use our reader for the next calls. This may be a problem in case of multi
         * threaded execution of JUnit tests!
         */
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, br);
        // check, if the correct items are returned
        final String header = "header";
        final List<String> displayNames = new ArrayList<String>();
        displayNames.add("nameTest1");
        displayNames.add("nameTest2");
        final List<String> items = new ArrayList<String>();
        items.add("itemTest1");
        items.add("itemTest2");
        Assert.assertEquals("itemTest1", ConsoleUiUtils.selectItem(header, displayNames, items));
        Assert.assertEquals("itemTest2", ConsoleUiUtils.selectItem(header, displayNames, items));
    }

    /**
     * Verifies, that an illegal argument exception is thrown, if more items than display names are defined.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSelectItemKeyIsNotSetMoreItemsThanNames()
    {
        final String header = "header";
        final List<String> displayNames = new ArrayList<String>();
        displayNames.add("nameTest1");
        final List<String> items = new ArrayList<String>();
        items.add("itemTest1");
        items.add("itemTest2");
        ConsoleUiUtils.selectItem(header, displayNames, items);
    }

    /**
     * Verifies that the right object is returned, if the keys are set.
     */
    @Test
    public void testSelectItemKeysAreSet()
    {
        /* The name of the static field to access. */
        final String fieldName = "STDIN_READER";
        /*
         * The input for the test, with second\nfirst representing the different choices which have to be separated by
         * line breaks.
         */
        final byte[] input = "second\nfirst".getBytes();
        /* The new reader from which to consume data for the test. */
        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input)));
        /*
         * Manipulating the tested class to use our reader for the next calls. This may be a problem in case of multi
         * threaded execution of JUnit tests!
         */
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, br);
        // check, if the correct items are returned
        final String header = "header";
        final List<String> keys = new ArrayList<String>();
        keys.add("first");
        keys.add("second");
        final List<String> displayNames = new ArrayList<String>();
        displayNames.add("nameTest1");
        displayNames.add("nameTest2");
        final List<String> items = new ArrayList<String>();
        items.add("itemTest1");
        items.add("itemTest2");
        // Notice, that the first input is 'second' and the second input is 'first'
        Assert.assertEquals("itemTest2", ConsoleUiUtils.selectItem(header, keys, displayNames, items));
        Assert.assertEquals("itemTest1", ConsoleUiUtils.selectItem(header, keys, displayNames, items));
    }

    /**
     * Verifies, that an illegal argument exception is thrown, if more display names than keys are defined.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSelectItemMoreNamesThanKeys()
    {
        final String header = "header";
        final List<String> keys = new ArrayList<String>();
        keys.add("first");
        final List<String> displayNames = new ArrayList<String>();
        displayNames.add("nameTest1");
        displayNames.add("nameTest2");
        final List<String> items = new ArrayList<String>();
        items.add("itemTest1");
        items.add("itemTest2");
        ConsoleUiUtils.selectItem(header, keys, displayNames, items);
    }

    /**
     * Verifies, that an illegal argument exception is thrown, if more items than keys are defined.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSelectItemMoreItemsThanKeys()
    {
        final String header = "header";
        final List<String> keys = new ArrayList<String>();
        keys.add("first");
        final List<String> displayNames = new ArrayList<String>();
        displayNames.add("nameTest1");
        final List<String> items = new ArrayList<String>();
        items.add("itemTest1");
        items.add("itemTest2");
        ConsoleUiUtils.selectItem(header, keys, displayNames, items);
    }

    private List<Integer> doMultiselection(final String input, final List<Integer> items, final boolean allInsteadOne)
    {
        final String fieldName = "STDIN_READER";
        final String header = "Choices";
        final byte[] in = input.getBytes();
        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in)));
        ReflectionUtils.writeStaticField(ConsoleUiUtils.class, fieldName, br);
        final List<String> displayNames = new ArrayList<String>(Arrays.asList("item"));
        final List<Integer> result = ConsoleUiUtils.multiSelectItems(header, displayNames, items, allInsteadOne);
        displayNames.remove(0);
        return result;
    }

    private static class MyEofHandler implements EofHandler
    {
        @Override
        public void onEof()
        {
        }
    }
}
