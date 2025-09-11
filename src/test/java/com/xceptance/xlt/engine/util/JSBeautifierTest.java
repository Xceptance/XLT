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
package com.xceptance.xlt.engine.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link JSBeautifier}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class JSBeautifierTest
{
    @Test
    public void testBeautify() throws Throwable
    {
        final String input = "var a,v,x,d=e,f=function(){v=a;return false};return v";
        final String result = JSBeautifier.beautify(input);
        Assert.assertNotNull(result);
        Assert.assertFalse("Code not beautified", input.equals(result));
        Assert.assertTrue("Insufficient lines of beautified code:\n" + result, result.split("[\\r\\n]+").length > 6);
    }

    @Test
    public void testBeautifyUnchanged() throws Throwable
    {
        final String input1 = "";
        Assert.assertEquals(input1, JSBeautifier.beautify(input1));

        final String input2 = "var x, y, z;";
        Assert.assertEquals(input2, JSBeautifier.beautify(input2));
    }
}
