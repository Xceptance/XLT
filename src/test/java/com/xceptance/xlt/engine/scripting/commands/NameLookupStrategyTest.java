package com.xceptance.xlt.engine.scripting.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.xceptance.xlt.engine.scripting.util.ReplayUtils;

public class NameLookupStrategyTest
{
    @Test
    public void testParseAttributes()
    {
        // name only
        processParsing("foo", "foo", null, null);

        // single values with value-key
        processParsing("foo index=23 value=bar", "foo", "bar", "23");// index middle
        processParsing("foo value=bar index=23", "foo", "bar", "23");// index end

        // single values without value-key
        processParsing("foo index=23 bar", "foo", "bar", "23");// index middle
        processParsing("foo bar index=23", "foo", "bar", "23");// index end

        // multiple values with value-key
        processParsing("foo index=23 value=bar1 bar2", "foo", "bar1 bar2", "23");// index middle
        processParsing("foo value=bar1 bar2 index=23", "foo", "bar1 bar2", "23");// index end

        // multiple values without value-key
        processParsing("foo index=23 bar1 bar2", "foo", "bar1 bar2", "23");// index middle
        processParsing("foo bar1 bar2 index=23", "foo", "bar1 bar2", "23");// index end

        // single values, no index
        processParsing("foo value=bar", "foo", "bar", null);
        processParsing("foo bar", "foo", "bar", null);

        // single values, no value
        processParsing("foo index=23", "foo", null, "23");

        // multiple values without value-key, no index
        processParsing("foo bar1 bar2", "foo", "bar1 bar2", null);

        // leading/trailing spaces in value
        processParsing("foo value= bar ", "foo", " bar ", null);
        processParsing("foo  bar ", "foo", " bar ", null);
        processParsing("foo  bar1 bar2 ", "foo", " bar1 bar2 ", null);
        processParsing("foo   bar1  bar2  ", "foo", "  bar1  bar2  ", null);

        processParsing("foo value= bar  index=23", "foo", " bar ", "23");
        processParsing("foo  bar  index=23", "foo", " bar ", "23");
        processParsing("foo  bar1 bar2  index=23", "foo", " bar1 bar2 ", "23");
        processParsing("foo   bar1  bar2   index=23", "foo", "  bar1  bar2  ", "23");

        processParsing("foo index=23 value= bar ", "foo", " bar ", "23");
        processParsing("foo index=23  bar ", "foo", " bar ", "23");
        processParsing("foo index=23  bar1 bar2 ", "foo", " bar1 bar2 ", "23");
        processParsing("foo index=23   bar1  bar2  ", "foo", "  bar1  bar2  ", "23");

        // empty value or whitespaces only value
        processParsing("foo ", "foo", "", null);
        processParsing("foo  ", "foo", " ", null);
        processParsing("foo   ", "foo", "  ", null);

        processParsing("foo value=", "foo", "", null);
        processParsing("foo value= ", "foo", " ", null);
        processParsing("foo value=  ", "foo", "  ", null);

        processParsing("foo  index=23", "foo", "", "23");
        processParsing("foo   index=23", "foo", " ", "23");
        processParsing("foo    index=23", "foo", "  ", "23");

        processParsing("foo value= index=23", "foo", "", "23");
        processParsing("foo value=  index=23", "foo", " ", "23");
        processParsing("foo value=   index=23", "foo", "  ", "23");

        processParsing("foo index=23 ", "foo", "", "23");
        processParsing("foo index=23  ", "foo", " ", "23");
        processParsing("foo index=23   ", "foo", "  ", "23");

        processParsing("foo index=23 value=", "foo", "", "23");
        processParsing("foo index=23 value= ", "foo", " ", "23");
        processParsing("foo index=23 value=  ", "foo", "  ", "23");
    }

    private void processParsing(final String input, final String expectedNameValue, final String excpectedValueValue,
                                final String excpectedIndexValue)
    {
        final Map<String, String> results = ReplayUtils.parseAttributes(input);
        validateResult(results, expectedNameValue, excpectedValueValue, excpectedIndexValue);
    }

    private void validateResult(final Map<String, String> results, final String expectedNameValue, final String excpectedValueValue,
                                final String expectedIndexValue)
    {
        assertNotNull(results);
        assertTrue(results.size() > 0);

        final String resultedNameValue = results.get("name");
        assertEquals(expectedNameValue, resultedNameValue);

        if (excpectedValueValue != null)
        {
            final String resultedValueValue = results.get("value");
            assertEquals(excpectedValueValue, resultedValueValue);
        }

        if (expectedIndexValue != null)
        {
            final String resultedIndexValue = results.get("index");
            assertEquals(expectedIndexValue, resultedIndexValue);
        }
    }

}
