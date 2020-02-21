package com.xceptance.xlt.showcases.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility method used for handling with properties.
 */
public class PropertyUtils
{
    /**
     * helper method to get a property as list
     * 
     * @param propertyValue
     *            the name of the property
     * @return if property or property value isn't set an empty list otherwise a list with the values
     */
    public static List<String> propertyToList(final String propertyValue)
    {
        return propertyToList(propertyValue, "\\|");
    }

    public static List<String> propertyToList(final String propertyValue, final String deliminator)
    {
        if (propertyValue.equals(""))
        {
            return new LinkedList<String>();
        }
        else
        {
            return Arrays.asList(propertyValue.split(deliminator));
        }
    }
}
