package com.xceptance.common.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration utility class.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AbstractConfiguration
{
    /**
     * Format of error message used when parsing of a certain property value failed.
     */
    private static final String PROPERTY_PARSING_ERROR_FORMAT = "The value '%s' of property '%s' cannot be resolved to a %s.";

    /**
     * The internal store of properties.
     */
    private final Properties props = new Properties();

    /**
     * Adds the properties from the given property set to the internal set of properties.
     * 
     * @param properties
     *            the property set with the additional properties
     */
    public void addProperties(final Properties properties)
    {
        props.putAll(properties);
    }

    /**
     * Returns the value of the given property converted to a class object. If the property value cannot be found, an
     * exception will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getClassProperty(String, Class)
     */
    public Class<?> getClassProperty(final String key)
    {
        final String s = getRequiredProperty(key);

        return parseClassName(key, s);
    }

    /**
     * Returns the value of the given property converted to a class object. If the property value cannot be found, the
     * specified default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public Class<?> getClassProperty(final String key, final Class<?> defaultValue)
    {
        final String s = getProperty(key);
        return s == null ? defaultValue : parseClassName(key, s);
    }

    /**
     * Returns the value of the given property converted to a boolean. If the property value cannot be found, an
     * exception will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getBooleanProperty(String, boolean)
     */
    public boolean getBooleanProperty(final String key)
    {
        final String s = getRequiredProperty(key);

        return Boolean.valueOf(s);
    }

    /**
     * Returns the value of the given property converted to a boolean. If the property value cannot be found, the
     * specified default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public boolean getBooleanProperty(final String key, final boolean defaultValue)
    {
        final String s = getProperty(key);
        return s == null ? defaultValue : Boolean.valueOf(s);
    }

    /**
     * Returns the value from the argument enum type whose name matches, regardless of the case, the value of the
     * property with the argument key.
     * 
     * @param enumType
     *            the type of the enum from which to return a value
     * @param key
     *            the name of the property whose value to use
     * @param defaultValue
     *            the value to return if the property has not been set, may be <code>null</code>
     * @return the enum value of the argument enum as described above or the argument defaultValue if the property for
     *         the argument key has not been set
     */
    public <T extends Enum<T>> T getEnumProperty(final Class<T> enumType, final String key, final T defaultValue)
    {
        final String value = getStringProperty(key, null);
        return StringUtils.isNotBlank(value) ? Enum.valueOf(enumType, value.toUpperCase()) : defaultValue;
    }

    /**
     * Returns the value of the given property converted to a file object. If the property value cannot be found, an
     * exception will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getFileProperty(String, File)
     */
    public File getFileProperty(final String key)
    {
        final String s = getRequiredProperty(key);

        return parseFileName(key, s);
    }

    /**
     * Returns the value of the given property converted to a file object. If the property value cannot be found, the
     * specified default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public File getFileProperty(final String key, final File defaultValue)
    {
        final String s = getProperty(key);
        return s == null ? defaultValue : parseFileName(key, s);
    }

    /**
     * Returns the value of the given property converted to an integer. If the property value cannot be found, an
     * exception will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getIntProperty(String, int)
     */
    public int getIntProperty(final String key)
    {
        final String value = getRequiredProperty(key);

        return parseInt(key, value);
    }

    /**
     * Returns the value of the given property converted to an integer. If the property value cannot be found, the
     * specified default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public int getIntProperty(final String key, final int defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseInt(key, value);
    }

    /**
     * Returns the value of the given property converted to a long. If the property value cannot be found, an exception
     * will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getIntProperty(String, int)
     */
    public long getLongProperty(final String key)
    {
        final String value = getRequiredProperty(key);

        return parseLong(key, value);
    }

    /**
     * Returns the value of the given property converted to a long. If the property value cannot be found, the specified
     * default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public long getLongProperty(final String key, final long defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseLong(key, value);
    }

    /**
     * Returns the value of the given property converted to a double. An exception will be thrown if the property value
     * cannot be found.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getDoubleProperty(String, int)
     */
    public double getDoubleProperty(final String key)
    {
        final String value = getRequiredProperty(key);

        return parseDouble(key, value);
    }

    /**
     * Returns the value of the given property converted to a double. The specified default value is returned if the
     * property value cannot be found.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public double getDoubleProperty(final String key, final double defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseDouble(key, value);
    }

    /**
     * Returns the raw properties.
     * 
     * @return the properties
     */
    public Properties getProperties()
    {
        return props;
    }

    /**
     * Returns the value of the given property.
     * 
     * @param key
     *            the property name
     * @return the property value
     */
    private String getProperty(final String key)
    {
        final String propVal = props.getProperty(key);
        return propVal == null ? null : PropertiesUtils.substituteVariables(propVal.trim(), props);
    }

    /**
     * Scans the properties for keys that starts with the given prefix. For each matching key, the prefix is stripped
     * from the key. Any potential suffix is also cut off. The set of keys found this way is returned.
     * <p>
     * For example:
     * 
     * <pre>
     *     foo = 0
     *     foo.bar.baz = 0
     *     foo.bar.baz.bum = 0
     *     foo.bar.bum = 0
     * </pre>
     * 
     * If the prefix was "foo.bar.", this method would return "baz" and "bum".
     * 
     * @param prefix
     *            the property name prefix
     * @return the set of keys found
     */
    public Set<String> getPropertyKeyFragment(final String prefix)
    {
        final Set<String> keysWithPrefix = getPropertyKeysWithPrefix(prefix);

        final Set<String> result = new HashSet<String>();
        final int prefixLength = prefix.length();

        for (final String s : keysWithPrefix)
        {
            String fragment = s.substring(prefixLength);
            final int periodIdx = fragment.indexOf('.');
            if (periodIdx >= 0)
            {
                fragment = fragment.substring(0, periodIdx);
            }
            result.add(fragment);
        }

        return result;
    }

    /**
     * Scans the properties for keys that starts with the given prefix. For each matching key, the prefix is stripped
     * from the key. Any potential suffix is also cut off. The set of keys found this way is returned.
     * <p>
     * For example:
     * 
     * <pre>
     *     foo = 0
     *     foo.bar.baz = 0
     *     foo.bar.baz.bum = 0
     *     foo.bar.bum = 0
     * </pre>
     * 
     * If the prefix was "foo.bar.", this method would return "baz" and "bum".
     * 
     * @param prefix
     *            the property name prefix
     * @return the set of keys found
     */
    public Set<String> getPropertyKeyFragments(final String prefix)
    {
        final Set<String> keysWithPrefix = getPropertyKeysWithPrefix(prefix);

        final Set<String> result = new HashSet<String>();
        final int prefixLength = prefix.length();

        for (final String s : keysWithPrefix)
        {
            String fragment = s.substring(prefixLength);
            final int periodIdx = fragment.indexOf('.');
            if (periodIdx >= 0)
            {
                fragment = fragment.substring(0, periodIdx);
            }
            result.add(fragment);
        }

        return result;
    }

    /**
     * Scans the properties for keys that starts with the given prefix. For each matching key, whether the prefix nor
     * any potential suffix will be cut off.
     * <p>
     * For example:
     * 
     * <pre>
     *     foo = 0
     *     foo.bar.baz = 0
     *     foo.bar.baz.bum = 0
     *     foo.bar.bum = 0
     * </pre>
     * 
     * If the prefix was "foo.bar.", this method would return "foo.bar.baz", "foo.bar.baz.bum" and "foo.bar.bum".
     * 
     * @param prefix
     *            the property name prefix
     * @return the set of keys found
     */
    public Set<String> getPropertyKeysWithPrefix(final String prefix)
    {
        ParameterCheckUtils.isNotNullOrEmpty(prefix, "prefix");

        final Set<String> set = new HashSet<String>();

        for (final Object o : props.keySet())
        {
            final String key = (String) o;
            if (key.startsWith(prefix))
            {
                set.add(key);
            }
        }

        return set;
    }

    /**
     * Returns the value of the given property. If the property value cannot be found, an exception will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     */
    private String getRequiredProperty(final String key)
    {
        final String propVal = getProperty(key);
        if (propVal == null)
        {
            throw new RuntimeException("No value found for required property: " + key);
        }

        return propVal;
    }

    /**
     * Returns the value of the given property as string value. If the property value cannot be found, an exception will
     * be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getStringProperty(String, String)
     */
    public String getStringProperty(final String key)
    {
        return getRequiredProperty(key);
    }

    /**
     * Returns the value of the given property. If the property value cannot be found, the specified default value is
     * returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public String getStringProperty(final String key, final String defaultValue)
    {
        final String propVal = getProperty(key);
        return propVal == null ? defaultValue : propVal;
    }

    /**
     * Returns the value of the given property converted to a URL. If the property value cannot be found, an exception
     * will be thrown.
     * 
     * @param key
     *            the property name
     * @return the property value
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getUrlProperty(String, URL)
     */
    public URL getUrlProperty(final String key)
    {
        return parseUrl(key, getRequiredProperty(key));
    }

    /**
     * Returns the value of the given property converted to a URL. If the property value cannot be found, the specified
     * default value is returned instead.
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public URL getUrlProperty(final String key, final URL defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseUrl(key, value);
    }

    /**
     * Returns the value of the given property converted to an URL. If the property value cannot be found, an exception
     * will be thrown.
     * <p>
     * This is a replacement for the {@link #getUrlProperty(String)} which is buggy due to a bug in
     * {@link URL#toString()}.
     * </p>
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     * @throws RuntimeException
     *             if there is no property configured for the argument key
     */
    public URI getUriProperty(final String key)
    {
        return parseUri(key, getRequiredProperty(key));
    }

    /**
     * Returns the value of the given property converted to an URL. The specified default value is returned if the
     * property value cannot be found.
     * <p>
     * This is a replacement for the {@link #getUrlProperty(String)} which is buggy due to a bug in
     * {@link URL#toString()}.
     * </p>
     * 
     * @param key
     *            the property name
     * @param defaultValue
     *            the default property value
     * @return the property value
     */
    public URI getUriProperty(final String key, final URI defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseUri(key, value);
    }

    /**
     * Returns the value of the given time period property converted to seconds. When property lookup failed, the given
     * default value is returned.
     * 
     * @param key
     *            property name
     * @param defaultValue
     *            default value
     * @return value of property
     */
    public int getTimePeriodProperty(final String key, final int defaultValue)
    {
        final String value = getProperty(key);
        return value == null ? defaultValue : parseTimePeriod(key, value);
    }

    /**
     * Returns the value of the given time period property converted to seconds. If the property value cannot be found,
     * an exception will be thrown.
     * 
     * @param key
     *            property name
     * @return value of property
     * @throws RuntimeException
     *             if the value cannot be found
     * @see #getTimePeriodProperty(String, int)
     */
    public int getTimePeriodProperty(final String key)
    {
        return parseTimePeriod(key, getRequiredProperty(key));
    }

    /**
     * Loads the properties from the given file to the internal properties object.
     * 
     * @param propertiesFile
     *            the properties file
     * @throws IOException
     *             if an I/O error occurred
     */
    public void loadProperties(final File propertiesFile) throws IOException
    {
        PropertiesUtils.loadProperties(propertiesFile, props);

        // assure that system properties always override file properties
        // props.putAll(System.getProperties());
    }

    /**
     * Resolves the given string value to a class.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting class
     * @throws RuntimeException
     *             if the string value cannot be resolved to a class
     */
    private Class<?> parseClassName(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseClass(value);
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "class"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "class"));
        }
    }

    /**
     * Resolves the given string value to a file.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting file
     * @throws RuntimeException
     *             if the string value cannot be resolved to a file name
     */
    private File parseFileName(final String key, final String value)
    {
        if (value.length() == 0)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "file"));
        }
        return new File(value);
    }

    /**
     * Parses the given string value as an int and returns its value.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting int
     * @throws RuntimeException
     *             if the string value cannot be converted to an int
     */
    protected int parseInt(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseInt(value);
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "integer"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "integer"));
        }
    }

    /**
     * Parses the given string value as aa long and returns its value.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting long
     * @throws RuntimeException
     *             if the string value cannot be converted to a long
     */
    protected long parseLong(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseLong(value);
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "long"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "long"));
        }
    }

    /**
     * Parses the given string value as a double and returns its value.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting double
     * @throws RuntimeException
     *             if the string value cannot be converted to an double
     */
    protected double parseDouble(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseDouble(value);
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "double"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "double"));
        }
    }

    /**
     * Parses the given string value as a URL and returns it.
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting URL
     * @throws RuntimeException
     *             if the string value cannot be converted to a URL
     */
    private URL parseUrl(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseURI(value).toURL();
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "URL"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "URL"));
        }
        catch (final MalformedURLException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "URL"));
        }
    }

    /**
     * Parses the given string value as an URI and returns it. This method is intended as a replacement for the buggy
     * URL class (if creating an URL with new URL("file:///tmp") and calling URL#toString the string representation
     * misses the two slashes for the protocol).
     * 
     * @param key
     *            the property name
     * @param value
     *            the property value
     * @return the resulting URL
     * @throws RuntimeException
     *             if the string value cannot be converted to an URI
     */
    private URI parseUri(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseURI(value);
        }
        catch (final ParseException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "URI"));
        }
        catch (final IllegalArgumentException ex)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "URI"));
        }
    }

    /**
     * Parses the given property value for the given property name and return its value in total number of seconds.
     * 
     * @param key
     *            property name
     * @param value
     *            property value as time period string
     * @return value of time period in total number of seconds
     * @throws RuntimeException
     *             thrown when the given property value cannot be parsed as time period string
     */
    private int parseTimePeriod(final String key, final String value)
    {
        try
        {
            return ParseUtils.parseTimePeriod(value);
        }
        catch (final ParseException e)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "time period"));
        }
        catch (final IllegalArgumentException e)
        {
            throw new RuntimeException(String.format(PROPERTY_PARSING_ERROR_FORMAT, value, key, "time period"));
        }
    }
}
