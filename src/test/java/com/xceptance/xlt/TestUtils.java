package com.xceptance.xlt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.xceptance.common.lang.XltCharBuffer;

public class TestUtils
{
    /**
     * Just to transform types
     */
    public static List<XltCharBuffer> toListOfXltCharBuffer(final String[] src)
    {
        return Arrays.stream(src).map(s -> XltCharBuffer.valueOf(s)).collect(Collectors.toList());
    }
}
