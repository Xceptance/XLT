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
package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentUsersTableTest
{
    @Before
    public void setup()
    {
        final ConcurrentUsersTable table = ConcurrentUsersTable.getInstance();
        table.clear();

        // Second ------ 01234567890123
        // ----------------------------
        // TAuthor-0 --- 00000001110011
        // TAuthor-1 --- 00011100000111
        // TVisitor-0 -- 00000011111110
        // ----------------------------
        // Sum --------- 00011112221232

        table.recordUserActivity(7999, 9999, "TAuthor", "0");
        table.recordUserActivity(12000, 13999, "TAuthor", "0");

        table.recordUserActivity(3000, 5500, "TAuthor", "1");
        table.recordUserActivity(11000, 13500, "TAuthor", "1");

        table.recordUserActivity(6000, 9100, "TVisitor", "0"); // ends in second 9
        table.recordUserActivity(9500, 12500, "TVisitor", "0"); // starts in second 9
    }

    /**
     * Tests {@link ConcurrentUsersTable#getConcurrentUsersValueSet(String)}.
     */
    @Test
    public void test_getConcurrentUsersValueSet_byUserName()
    {
        ValueSet valueSet = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet("TAuthor");
        int[] values = valueSet.getValues();

        Assert.assertEquals("Wrong first second", 3, valueSet.getFirstSecond());
        Assert.assertEquals("Wrong last second", 13, valueSet.getLastSecond());
        Assert.assertEquals("Wrong value count second", 11, valueSet.getValueCount());
        Assert.assertEquals("Wrong users count", 1, values[0]); // 3s
        Assert.assertEquals("Wrong users count", 1, values[1]); // 4s
        Assert.assertEquals("Wrong users count", 1, values[2]); // 5s
        Assert.assertEquals("Wrong users count", 0, values[3]); // 6s
        Assert.assertEquals("Wrong users count", 1, values[4]); // 7s
        Assert.assertEquals("Wrong users count", 1, values[5]); // 8s
        Assert.assertEquals("Wrong users count", 1, values[6]); // 9s
        Assert.assertEquals("Wrong users count", 0, values[7]); // 10s
        Assert.assertEquals("Wrong users count", 1, values[8]); // 11s
        Assert.assertEquals("Wrong users count", 2, values[9]); // 12s
        Assert.assertEquals("Wrong users count", 2, values[10]); // 13s

        valueSet = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet("TVisitor");
        values = valueSet.getValues();

        Assert.assertEquals("Wrong first second", 6, valueSet.getFirstSecond());
        Assert.assertEquals("Wrong last second", 12, valueSet.getLastSecond());
        Assert.assertEquals("Wrong value count second", 7, valueSet.getValueCount());
        Assert.assertEquals("Wrong users count", 1, values[0]); // 6s
        Assert.assertEquals("Wrong users count", 1, values[1]); // 7s
        Assert.assertEquals("Wrong users count", 1, values[2]); // 8s
        Assert.assertEquals("Wrong users count", 1, values[3]); // 9s
        Assert.assertEquals("Wrong users count", 1, values[4]); // 10s
        Assert.assertEquals("Wrong users count", 1, values[5]); // 11s
        Assert.assertEquals("Wrong users count", 1, values[6]); // 12s
    }

    /**
     * Tests {@link ConcurrentUsersTable#getConcurrentUsersValueSet()}.
     */
    @Test
    public void test_getConcurrentUsersValueSet_all()
    {
        ValueSet valueSet = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet();
        int[] values = valueSet.getValues();

        Assert.assertEquals("Wrong first second", 3, valueSet.getFirstSecond());
        Assert.assertEquals("Wrong last second", 13, valueSet.getLastSecond());
        Assert.assertEquals("Wrong value count second", 18, valueSet.getValueCount());
        Assert.assertEquals("Wrong users count", 1, values[0]); // 3s
        Assert.assertEquals("Wrong users count", 1, values[1]); // 4s
        Assert.assertEquals("Wrong users count", 1, values[2]); // 5s
        Assert.assertEquals("Wrong users count", 1, values[3]); // 6s
        Assert.assertEquals("Wrong users count", 2, values[4]); // 7s
        Assert.assertEquals("Wrong users count", 2, values[5]); // 8s
        Assert.assertEquals("Wrong users count", 2, values[6]); // 9s
        Assert.assertEquals("Wrong users count", 1, values[7]); // 10s
        Assert.assertEquals("Wrong users count", 2, values[8]); // 11s
        Assert.assertEquals("Wrong users count", 3, values[9]); // 12s
        Assert.assertEquals("Wrong users count", 2, values[10]); // 13s
    }

    /**
     * Tests {@link ConcurrentUsersTable#clear()}.
     */
    @Test
    public void test_clear()
    {
        // clear the bits
        ConcurrentUsersTable.getInstance().clear();
        ValueSet valueSet = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet();

        Assert.assertEquals("Wrong value count", 0, valueSet.getValueCount());
    }

    /**
     * Tests {@link ConcurrentUsersTable#toString()}.
     */
    @Test
    public void test_toString()
    {
        final ConcurrentUsersTable table = ConcurrentUsersTable.getInstance();

        Assert.assertEquals("Wrong toString output",
                            "TAuthor-0|00001|11001|1\n" + "TAuthor-1|11100|00011|1\n" + "TVisitor-0|00011|11111\n", table.toString());
    }
}
