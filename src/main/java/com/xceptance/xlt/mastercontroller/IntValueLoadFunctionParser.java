package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import com.xceptance.common.util.ParseUtils;

/**
 * A load function parser that reads and returns the value of a time/value pair as an int.
 */
public class IntValueLoadFunctionParser extends AbstractLoadFunctionParser
{
    /**
     * {@inheritDoc}
     */
    protected int parseValue(final String s) throws ParseException
    {
        return ParseUtils.parseInt(s);
    }
}
