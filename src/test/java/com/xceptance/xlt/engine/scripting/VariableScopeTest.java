/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests implementation of {@link VariableScope}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Marcel Pfotenhauer (Xceptance Software Technologies GmbH)
 */
public class VariableScopeTest
{
    private final String testText = "Hi ${name}! This is ${foo} (yes, it's ${foo}) - I can see $$ in your eyes, $${name}";

    @Test
    public void testResolve() throws Throwable
    {
        final HashMap<String, String> scope1Data = new HashMap<String, String>();
        final HashMap<String, String> scope2Data = new HashMap<String, String>();

        final VariableScope scope1 = new VariableScope(scope1Data, null);
        final VariableScope scope2 = new VariableScope(scope2Data, scope1);

        scope1Data.put("foo", "boo");
        scope2Data.put("foo", "bar");

        final String s1 = scope1.resolve(testText);
        final String s2 = scope2.resolve(testText);

        Assert.assertEquals("Hi ${name}! This is boo (yes, it's boo) - I can see $ in your eyes, ${name}", s1);
        Assert.assertEquals(s1, s2);

        scope1Data.remove("foo");

        Assert.assertEquals("Hi ${name}! This is bar (yes, it's bar) - I can see $ in your eyes, ${name}", scope2.resolve(testText));

        scope1Data.put("name", "${foo} bar");

        Assert.assertEquals("Hi bar bar! This is bar (yes, it's bar) - I can see $ in your eyes, ${name}", scope2.resolve(testText));

        scope1Data.put("bar", "bli");

        Assert.assertEquals("bla bli blubb", scope2.resolve("bla ${${foo}} blubb"));

        Assert.assertEquals("baz", scope2.resolve("baz"));
    }

    @Test
    public void testReolve_InvalidChars() throws Throwable
    {
        final HashMap<String, String> data = new HashMap<String, String>();
        data.put("foö", "bar");

        final VariableScope scope = new VariableScope(data, null);
        Assert.assertEquals("a ${foö}", scope.resolve("a ${foö}"));
        Assert.assertEquals("a ${foö}}", scope.resolve("a ${foö}}"));
    }

    @Test
    public void testResolveKey() throws Throwable
    {
        final HashMap<String, String> scope1Data = new HashMap<String, String>();

        final VariableScope scope1 = new VariableScope(scope1Data, null);

        scope1Data.put("foo", "bar");
        scope1Data.put("bar", "${bar}");

        final String s0 = scope1.resolveKey(null);
        final String s1 = scope1.resolveKey("foo");
        final String s2 = scope1.resolveKey("${foo}");
        final String s3 = scope1.resolveKey("baz");
        final String s4 = scope1.resolveKey("bar");

        Assert.assertNull(s0);
        Assert.assertEquals("bar", s1);
        Assert.assertEquals(s1, s2);
        Assert.assertNull(s3);
        Assert.assertNull(s4);
    }
}
