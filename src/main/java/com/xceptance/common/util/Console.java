package com.xceptance.common.util;

/**
 * Just a simple class to help with the output to the console and at the same time to the 
 * logs. It can also do some pretty printing of needed and do empty lines and such which 
 * is not what a log needs.
 * 
 * This could be a real terminal or console with colors maybe or things like that... 
 * but for now, it is way too much.
 * 
 * @author rschwietzke
 *
 */
public class Console
{
    private static final int LENGTH = 80;
    
    public static String startSection(final String topic)
    {
        final StringBuilder s = new StringBuilder();
        s.append(topic);
        
        return s.toString();
    }

    private static String bar(final boolean nlAfter)
    {
        final StringBuilder s = new StringBuilder();
        
        for (int i = 0; i < 80; i++)
        {
            s.append("-");
        }
        
        return nlAfter ? s.append("\n").toString() : s.toString();
    }
    
    public static String horizontalBar()
    {
        return bar(false);
    }
    
    public static String endSection()
    {
        return bar(true);
    }
}
