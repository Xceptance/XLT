package com.xceptance.xlt.engine.util;

import java.util.Arrays;

/**
 * This is a wrapper around string to cache the hashcode and 
 * change the equals comparison. This is a perfect candidate for Valhalla's
 * inline classes
 *  
 * @author rschwietzke
 *
 */
public class FastString
{
    private final int hashCode;
    private final String data;
    private final char[] charArray;
    
    public FastString(final String data, int hashCode)
    {
        this.data = data;
        this.hashCode = hashCode;
        this.charArray = data.toCharArray();
    }
    
    public FastString(final String data)
    {
        this.data = data;
        this.hashCode = hashCode();
        this.charArray = data.toCharArray();
    }
    
    @Override
    public String toString()
    {
        return data;
    }
    
    @Override
    public int hashCode()
    {
        if (hashCode != 0)
        {
            return hashCode;
        }
        
        final int length = data.length();

        int h = 0;
        int i0 = 0;
        int i1 = 1;
        int i2 = 2;
        while (i2 < length) {
            h = h * (31 * 31 * 31) + data.charAt(i0) * (31 * 31) + data.charAt(i1) * 31 + data.charAt(i2);
            i0 = i2 + 1;
            i1 = i0 + 1;
            i2 = i1 + 1;
        }
        if (i0 < length) {
            h = h * 31 + data.charAt(i0);
        }
        if (i1 < length) {
            h = h * 31 + data.charAt(i1);
        }
        
        return h;    
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }

        FastString other = (FastString) obj;
        if (hashCode != obj.hashCode())
        {
            return false;
        }
        
        return Arrays.equals(charArray, other.charArray);
    }
    
    
}
