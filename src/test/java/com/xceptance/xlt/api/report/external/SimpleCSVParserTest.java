package com.xceptance.xlt.api.report.external;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class SimpleCSVParserTest
{
    @Test(expected = IllegalStateException.class)
    public void testParse_noConfigSet()
    {
        final long time = 123000;
        final String line = time + ",0.1,0.2,0.3";

        final SimpleCsvParser scp = new SimpleCsvParser();
        scp.parse(line);
    }

    @Test
    public void testParse()
    {
        final long time = 123000;
        final String line = time + ",0.1,0.2,0.3";

        final SimpleCsvParser scp = new SimpleCsvParser();

        // now set the properties
        scp.setProperties(new Properties());

        // the separator property is empty
        scp.getProperties().setProperty("parser.csv.separator", "");

        ValueSet vs = scp.parse(line);
        Assert.assertTrue("No values expected", vs.getValues().isEmpty());

        // remove the separator property
        scp.getProperties().remove("parser.csv.separator");

        scp.setValueNames(new HashSet<String>(Arrays.asList(new String[]
            {
                "1", "3"
            })));
        vs = scp.parse(line);
        Assert.assertEquals(time, vs.getTime());
        Assert.assertEquals("", 2, vs.getValues().size());
        Assert.assertEquals(0.1, vs.getValues().get("1"));
        Assert.assertTrue("No value expected", vs.getValues().get("2") == null);
        Assert.assertEquals(0.3, vs.getValues().get("3"));
    }
}
