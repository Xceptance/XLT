package com.xceptance.xlt.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.xceptance.common.lang.XltCharBuffer;

/**
 * This is only a utility class for testing
 * 
 * @author rschwietzke
 *
 */
public class XltCharBufferUtil
{
    public static List<XltCharBuffer> toList(final List<String> list)
    {
        return list.stream().map(s -> new XltCharBuffer(s.toCharArray())).collect(Collectors.toList());
    }

    public static List<XltCharBuffer> toList(final String[] list)
    {
        return Arrays.stream(list).map(s -> new XltCharBuffer(s.toCharArray())).collect(Collectors.toList());
    }
}
