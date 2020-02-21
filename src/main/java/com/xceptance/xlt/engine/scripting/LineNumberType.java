package com.xceptance.xlt.engine.scripting;

import org.apache.commons.lang3.StringUtils;

/**
 * Type of line number.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public enum LineNumberType
{
    scriptdeveloper, file;

    /**
     * Get the type for given name.
     * 
     * @param name
     *            name of type
     * @return type with given name
     * @throws RuntimeException
     *             if there is no type mapped to the given name
     */
    public static LineNumberType get(final String name)
    {
        if (StringUtils.isNotBlank(name))
        {
            for (final LineNumberType type : values())
            {
                if (type.toString().equalsIgnoreCase(name))
                {
                    return type;
                }
            }
        }

        throw new RuntimeException("Unsupported line number type: " + name);
    }
}
