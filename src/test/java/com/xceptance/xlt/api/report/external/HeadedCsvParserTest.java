/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report.external;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class HeadedCsvParserTest
{
    @Test
    public void testParse()
    {
        final String headsLine = "time,first,second,third,fourth";
        final long time = 123000;
        final String line = time + "0.0,0.1,0.2,0.3,0.4,0.5";

        final TestHeadedCsvParser hcp = new TestHeadedCsvParser();
        hcp.setProperties(new Properties());

        Assert.assertEquals("There should not be any value names by now!", null, hcp.getValueNames());

        hcp.parse(headsLine);
        hcp.setValueNames(new HashSet<String>(Arrays.asList(new String[]
            {
                "first", "third"
            })));
        // Intentionally taking two different sets
        final Set<String> expected = new HashSet<String>(Arrays.asList(new String[]
            {
                "first", "third"
            }));

        Assert.assertEquals("Expected value names mismatch!", expected, hcp.getValueNames());
        Assert.assertEquals("Expected value name is not matched", "first", hcp.getName(1));
        Assert.assertEquals("Expected value name is not matched", "10", hcp.getName(10));

        final ValueSet vs = hcp.parse(line);
        Assert.assertEquals("Wrong number of values", expected.size(), vs.getValues().size());
        Assert.assertEquals(0.1, vs.getValues().get("first"));
        Assert.assertEquals(0.3, vs.getValues().get("third"));
    }

    /**
     * Test implementation just to make {@link #getName(int)} accessible without reflection.
     */
    private class TestHeadedCsvParser extends HeadedCsvParser
    {
        @Override
        protected String getName(final int i)
        {
            return super.getName(i);
        }
    }
}
