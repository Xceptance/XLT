package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import com.xceptance.common.util.ParseUtils;

/**
 * A load function parser that reads the value of a time/value pair as a double and returns it normalized as a
 * per mil value. For instance, a value of '1.5' will be converted to '1500'.
 */
public class DoubleValueLoadFunctionParser extends AbstractLoadFunctionParser
{
    /**
     * {@inheritDoc}
     */
    protected int parseValue(final String s) throws ParseException
    {
        return (int) Math.ceil(ParseUtils.parseDouble(s) * 1_000);
    }
}
