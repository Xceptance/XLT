package com.xceptance.common.util;

import it.unimi.dsi.util.FastRandom;

public class RandomUtils
{
    private final static String LOWERCHARS = "abcdefghijklmnopqrstuvwxyz";
    private final static String UPPERCHARS = LOWERCHARS.toUpperCase();
    private final static String CHARS = LOWERCHARS + UPPERCHARS;
    
    /**
     * Fixed length random string
     * @param random
     * @param length
     * @return
     */
    public static String randomString(final FastRandom random, final int length)
    {
        return randomString(random, length, length);
    }

    /**
     * Variable length random string
     * @param random
     * @param from
     * @param to
     * @return
     */
    public static String randomString(final FastRandom random, final int from, final int to)
    {
        final int length = random.nextInt(to - from + 1) + from;
        
        final StringBuilder sb = new StringBuilder(to);
        
        for (int i = 0; i < length; i++)
        {
            final int pos = random.nextInt(CHARS.length());
            sb.append(CHARS.charAt(pos));
        }
        
        return sb.toString();
    }
}
