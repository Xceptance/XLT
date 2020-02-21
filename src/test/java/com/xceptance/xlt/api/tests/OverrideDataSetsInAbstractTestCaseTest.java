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
