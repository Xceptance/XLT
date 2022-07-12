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

public class StringHasherTest
{
    @Test
    public void behavesLikeString()
    {
        String s1 = "http://www.com/foobar";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(XltCharBuffer.valueOf(s1), '#'));
    }

    @Test
    public void ignoresRemaining()
    {
        String s1 = "http://www.com/foobar";
        String s2 = "http://www.com/foobar#nothing";
        
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s2, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(XltCharBuffer.valueOf(s2), '#'));
    }
    
    @Test
    public void handlesEmpty()
    {
        String s1 = "";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(XltCharBuffer.valueOf(s1), '#'));
    }    

    @Test
    public void handlesOnlyLimited()
    {
        Assert.assertEquals("".hashCode(), StringHasher.hashCodeWithLimit("#", '#'));
        Assert.assertEquals("".hashCode(), StringHasher.hashCodeWithLimit(XltCharBuffer.valueOf("#"), '#'));
    }       
    
    @Test
    public void handlesLimiterAtTheEnd()
    {
        Assert.assertEquals("test".hashCode(), StringHasher.hashCodeWithLimit("test#", '#'));
        Assert.assertEquals("test".hashCode(), StringHasher.hashCodeWithLimit(XltCharBuffer.valueOf("test#"), '#'));
    }    
}
