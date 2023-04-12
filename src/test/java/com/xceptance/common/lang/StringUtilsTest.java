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
package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest
{
    /**
     * Constructor
     */
    @Test
    public void constructor()
    {
        ReflectionUtils.classHasOnlyPrivateConstructors(StringUtils.class);
    }

    /**
     * Test the CRC32 helper
     */
    @Test
    public final void testCrc32()
    {
        Assert.assertEquals("0", StringUtils.crc32(""));
        Assert.assertEquals("2580642306", StringUtils.crc32("My CRC32"));
        Assert.assertEquals("4007021204", StringUtils.crc32("My CRC33"));
    }

    /**
     * Test the CRC32 helper and secure exception behavior.
     */
    @Test(expected = NullPointerException.class)
    public final void testCrc32_Exception()
    {
        StringUtils.crc32(null);
    }

    /**
     * replace first occurrence
     */
    @Test
    public final void testReplaceFirst()
    {
        Assert.assertEquals("FOO is a test", StringUtils.replaceFirst("th1s is a test", "th[0-9]s", "FOO"));
        Assert.assertEquals("FOO is a th1s", StringUtils.replaceFirst("th1s is a th1s", "th[0-9]s", "FOO"));

        // we do not check the regexp compile storing here

        Assert.assertEquals("FOO", StringUtils.replaceFirst("th1s is a test", ".*", "FOO"));
        Assert.assertEquals("th1sFOOis a th1s", StringUtils.replaceFirst("th1s is a th1s", "\\s", "FOO"));
    }

    /**
     * Check for all replacement of
     */
    @Test
    public final void testReplaceAll()
    {
        Assert.assertEquals("FOO is a test", StringUtils.replaceAll("th1s is a test", "th[0-9]s", "FOO"));
        Assert.assertEquals("FOO is a FOO", StringUtils.replaceAll("th1s is a th1s", "th[0-9]s", "FOO"));

        // we do not check the regexp compile storing here

        final String s = "th1s is a test";

        // this is a little odd, FOOFOO as result, but that is the way the JDK does it
        Assert.assertEquals(s.replaceAll(".*", "FOO"), StringUtils.replaceAll(s, ".*", "FOO"));

        Assert.assertEquals("th1sFOOisFOOaFOOth1s", StringUtils.replaceAll("th1s is a th1s", "\\s", "FOO"));
    }

    @Test
    public final void testReplace()
    {
        Assert.assertEquals("My big C. And C", StringUtils.replace("My big c. And C", "c", "C"));
        Assert.assertEquals("My big C. And C", StringUtils.replace("My big c. And c", "c", "C"));
        Assert.assertEquals("My big d. And D", StringUtils.replace("My big d. And D", "c", "C"));
        Assert.assertEquals("My big CCC. And C", StringUtils.replace("My big ccc. And C", "c", "C"));
    }

    @Test
    public final void testSplitStringStringInt()
    {
        Assert.assertArrayEquals(new String[]
            {
                "a", "b,c,d,e,f"
            }, StringUtils.split("a,b,c,d,e,f", ",", 2));
        Assert.assertArrayEquals(new String[]
            {
                "a", "b", "c,d,e,f"
            }, StringUtils.split("a,b,c,d,e,f", ",", 3));
        Assert.assertArrayEquals(new String[]
            {
                "a", "b", "c", "d", "e,f"
            }, StringUtils.split("a,b,c,d,e,f", ",", 5));
        Assert.assertArrayEquals(new String[]
            {
                "a", "b", "c", "d", "e", "f"
            }, StringUtils.split("a,b,c,d,e,f", ",", 6));
        Assert.assertArrayEquals(new String[]
            {
                "a", "b", "c", "d", "e", "f"
            }, StringUtils.split("a,b,c,d,e,f", ",", 10));
    }

    @Test
    public final void testSplitStringString()
    {
        Assert.assertArrayEquals(new String[]
            {
                "foo", "bar"
            }, StringUtils.split("foo bar", " "));
        Assert.assertArrayEquals(new String[]
            {
                "foo ", "ar"
            }, StringUtils.split("foo bar", "b"));
        Assert.assertArrayEquals(new String[]
            {
                "foo bar"
            }, StringUtils.split("foo bar", ","));
    }

}
