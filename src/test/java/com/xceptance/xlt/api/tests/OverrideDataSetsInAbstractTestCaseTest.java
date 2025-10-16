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
package com.xceptance.xlt.api.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the testdata-override 'feature' as described in issue #2605.
 */
public class OverrideDataSetsInAbstractTestCaseTest extends AbstractTestCase
{

    private final Map<String, String> m;

    public OverrideDataSetsInAbstractTestCaseTest()
    {
        final HashMap<String, String> map = new HashMap<>();
        map.put("Foo", "BAR");
        map.put("baz", "bum");
        map.put("time", Long.toString(System.currentTimeMillis()));
        m = map;
        setTestDataSet(map);
    }

    @Test
    public void test()
    {
        final Map<String, String> dataSet = getTestDataSet();
        Assert.assertNotNull("Data set is null", dataSet);
        Assert.assertEquals(m.get("Foo"), dataSet.get("Foo"));
        Assert.assertEquals(m.get("baz"), dataSet.get("baz"));
        Assert.assertEquals(m.get("time"), dataSet.get("time"));
    }
}
