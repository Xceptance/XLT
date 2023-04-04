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
package com.xceptance.xlt.engine;

import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Script;
import org.htmlunit.corejs.javascript.Scriptable;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class XltScriptTest
{
    @Test
    public void test()
    {
        final String expected = "Artificial implementation only for testing purpose";
        final Script dummy = new Script()
        {
            @Override
            public Object exec(final Context cx, final Scriptable scope)
            {
                return null;
            }

            @Override
            public String toString()
            {
                return expected;
            }
        };
        final XltScript script = new XltScript(dummy, "bla");
        Assert.assertSame("Wrong wrapped script, ", dummy, script.getWrappedScript());
        Assert.assertEquals("Wrong source name, ", "bla", script.getSourceName());
        Assert.assertEquals("Script is not returned as expected", expected, script.toString());
    }
}
