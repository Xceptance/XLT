package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link DoubleValueLoadFunctionParser}. For more complex tests, we rely on
 * {@link IntValueLoadFunctionParserTest}.
 */
public class DoubleValueLoadFunctionParserTest
{
    @Test
    public void testParseLoadFunctionSimple() throws ParseException
    {
        final String loadFunctionProperty = "0/1 10/2.0 20/3.33 30/.05 40/.018 45/.0001";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                    {
                        0, 1000
                    },
                    {
                        10, 2000
                    },
                    {
                        20, 3330
                    },
                    {
                        30, 50
                    },
                    {
                        40, 18
                    },
                    {
                        45, 1
                    }
            };
        Assert.assertArrayEquals(expecteds, function);
    }
}
