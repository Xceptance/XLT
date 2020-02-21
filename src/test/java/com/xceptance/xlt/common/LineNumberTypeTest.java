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
