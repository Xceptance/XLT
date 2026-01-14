package com.xceptance.xlt.report.scorecard;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * Access to XLT properties from Groovy scorecard configurations.
 */
public class ScorecardProperties
{
    private final XltProperties properties;

    public ScorecardProperties()
    {
        this.properties = XltProperties.getInstance();
    }

    /**
     * Returns the property value for the given key.
     *
     * @param key
     *                the property key
     * @return the property value or null if not found
     */
    public String get(final String key)
    {
        return properties.getProperty(key);
    }

    /**
     * Returns the property value for the given key, or the default value if not found.
     *
     * @param key
     *                         the property key
     * @param defaultValue
     *                         the default value
     * @return the property value or default value
     */
    public String get(final String key, final String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns boolean value of a property.
     */
    public boolean getBoolean(final String key, final boolean defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns int value of a property.
     */
    public int getInt(final String key, final int defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns long value of a property.
     */
    public long getLong(final String key, final long defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }
}
