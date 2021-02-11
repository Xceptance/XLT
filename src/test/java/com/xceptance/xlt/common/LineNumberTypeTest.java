/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.common;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.TestWrapper;
import com.xceptance.xlt.engine.scripting.LineNumberType;

/**
 * @author Sebastian Oerding
 */
public class LineNumberTypeTest
{
    @Test
    public void testGetPositive()
    {
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.scriptdeveloper, LineNumberType.get("scriptdeveloper"));
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.scriptdeveloper, LineNumberType.get("ScriptDeveloper"));
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.scriptdeveloper, LineNumberType.get("SCRIPTDEVELOPER"));
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.file, LineNumberType.get("file"));
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.file, LineNumberType.get("File"));
        Assert.assertEquals("Getting wrong enum value, ", LineNumberType.file, LineNumberType.get("fIlE"));
    }

    @Test
    public void testGetNegative()
    {
        final String[] values = new String[]
            {
                "sciptDeveloper", "srciptDevloper", "Fil", "ile"
            };
        for (final String name : values)
        {
            new TestWrapper(RuntimeException.class, "Unsupported line number type: " + name, "Expected no linenumber type matching \"" +
                                                                                             name + "\"")
            {
                @Override
                protected void run()
                {
                    LineNumberType.get(name);
                }
            }.execute();
        }
    }
}
