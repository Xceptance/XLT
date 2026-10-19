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
package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;


public class StringHasherTest
{
    @Test
    public void behavesLikeString()
    {
        String s1 = "http://www.com/foobar";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(new StringBuilder(s1), '#'));
    }

    @Test
    public void ignoresRemaining()
    {
        String s1 = "http://www.com/foobar";
        String s2 = "http://www.com/foobar#nothing";
        
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s2, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(new StringBuilder(s2), '#'));
    }
    
    @Test
    public void handlesEmpty()
    {
        String s1 = "";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(new StringBuilder(s1), '#'));
    }    

    @Test
    public void handlesOnlyLimited()
    {
        Assert.assertEquals("".hashCode(), StringHasher.hashCodeWithLimit("#", '#'));
        Assert.assertEquals("".hashCode(), StringHasher.hashCodeWithLimit(new StringBuilder("#"), '#'));
    }       
    
    @Test
    public void handlesLimiterAtTheEnd()
    {
        Assert.assertEquals("test".hashCode(), StringHasher.hashCodeWithLimit("test#", '#'));
        Assert.assertEquals("test".hashCode(), StringHasher.hashCodeWithLimit(new StringBuilder("test#"), '#'));
    }

    @Test
    public void testByteSliceHashingAsciiAndUtf8Fallback()
    {
        Assert.assertNotNull(new StringHasher());

        // Empty slice (length 0)
        final byte[] sample = "https://example.com".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Assert.assertEquals(0, StringHasher.hashCodeWithLimit(sample, 0, 0, '#'));

        // ASCII slice with non-zero offset and no fragment
        final byte[] paddedNoFrag = "___https://example.com/shop?id=42___".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Assert.assertEquals("https://example.com/shop?id=42".hashCode(),
                            StringHasher.hashCodeWithLimit(paddedNoFrag, 3, 30, '#'));

        // ASCII slice with non-zero offset and #fragment (including non-ASCII after #fragment)
        final byte[] paddedWithFrag = "___https://example.com/shop?id=42#sect-ü___".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Assert.assertEquals("https://example.com/shop?id=42".hashCode(),
                            StringHasher.hashCodeWithLimit(paddedWithFrag, 3, paddedWithFrag.length - 6, '#'));

        // Slice starting immediately with limiter
        final byte[] onlyFrag = "#fragment".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Assert.assertEquals(0, StringHasher.hashCodeWithLimit(onlyFrag, 0, onlyFrag.length, '#'));

        // Non-ASCII UTF-8 characters before #fragment trigger exact UTF-8 String fallback
        final String utf8Base = "https://example.com/grüße/äöü?q=€";
        final byte[] paddedUtf8 = ("!!" + utf8Base + "#frag!!").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Assert.assertEquals(utf8Base.hashCode(),
                            StringHasher.hashCodeWithLimit(paddedUtf8, 2, paddedUtf8.length - 4, '#'));
    }
}
