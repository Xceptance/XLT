package com.xceptance.xlt.api.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;

import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * The property keeper. Loads and stores the properties of the entire tool. Single instance implementation.
 * <p>
 * The process of looking up a property uses multiple fall-backs. When resolving the value for the key "foo.bar", for
 * instance, the following effective keys are tried, in this order:
 * <ol>
 * <li>the test user name plus simple key, e.g. "TOrder.foo.bar"</li>
 * <li>the test class name plus simple key, e.g. "posters.loadtest.tests.TOrder.foo.bar"</li>
 * <li>the simple key, i.e. "foo.bar"</li>
 * </ol>
 * This multi-step process allows for test-user-specific or test-class-specific overrides of certain settings, while
 * falling back to the globally defined values if such specific settings are absent.
 */
public abstract class XltProperties
{
    /**
     * Returns the one and only XltProperties instance.
     * 
     * @return the XltProperties singleton
     */
    public static XltProperties getInstance()
    {
        return XltPropertiesImpl.getInstance();
    }

    /**
     * Resets the properties framework. This is mainly needed for testing.
     * 
     * @deprecated For internal use only.
     */
    @Deprecated
    public static synchronized void reset()
    {
        XltPropertiesImpl.reset();
    }

    /**
     * Checks whether there is a mapping for the specified key in this property list.
     * 
     * @param key
     *            the property key
     * @return <code>true</code> if there is a mapping, <code>false</code> otherwise
     */
    public abstract boolean containsKey(final String key);

    /**
     * Returns a copy of all the internally stored properties, with any placeholder resolved.
     * 
     * @return the properties
     */
    public abstract Properties getProperties();

    /**
     * Returns all properties whose name starts with the given domain key. The domain is stripped from the resulting
     * property names.
     * 
     * @param domainKey
     *            domain for the properties
     * @return a map with all matching properties
     */
    public abstract Map<String, String> getPropertiesForKey(final String domainKey);

    /**
     * Searches for the property with the specified key in this property list. The method returns null if the property
     * is not found.
     * 
     * @param key
     *            the property key
     * @return the value of the key
     */
    public abstract String getProperty(final String key);

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     * 
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as a boolean
     */
    public abstract boolean getProperty(final String key, final boolean defaultValue);

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     * 
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as an int
     */
    public abstract int getProperty(final String key, final int defaultValue);

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     * 
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as a long
     */
    public abstract long getProperty(final String key, final long defaultValue);

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found. The key is upper-cased before the property will be searched.
     * 
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key
     */
    public abstract String getProperty(final String key, final String defaultValue);

    /**
     * Returns one value of the given multi-value property. Multiple values are separated by comma, semicolon, or space.
     * The returned value is chosen randomly from the set of values.
     * 
     * @param key
     *            the name of the property
     * @param defaultValue
     *            the default property value (a multi-value)
     * @return one of the values, chosen randomly
     */
    public abstract String getPropertyRandomValue(final String key, final String defaultValue);

    /**
     * Returns the start time of the test in milliseconds since 1970.
     * 
     * @return the start time of the test in milliseconds
     */
    public abstract long getStartTime();

    /**
     * Returns the product version.
     * 
     * @return the version string, e.g. "1.1.0"
     */
    public abstract String getVersion();

    /**
     * Removes the property with the given key from the internal properties store.
     * 
     * @param key
     *            the property key
     */
    public abstract void removeProperty(final String key);

    /**
     * Defines a source for property data. If properties are already loaded, these new properties will be added. If a
     * property already exists it will be overwritten. Last one wins. Automatically adds java system properties
     * afterwards.
     * 
     * @param file
     *            the file that contains the properties to be loaded
     * @throws IOException
     *             thrown when opening the file or reading from the file failed
     */
    public abstract void setProperties(final File file) throws IOException;

    /**
     * Defines a source for property data. If properties are already loaded, these new properties will be added. If a
     * property already exists it will be overwritten. Last one wins. Automatically adds java system properties
     * afterwards.
     * 
     * @param file
     *            the file that contains the properties to be loaded
     * @throws IOException
     *             thrown when opening the file or reading from the file failed
     */
    public abstract void setProperties(final FileObject file) throws IOException;

    /**
     * Method for changing the properties during runtime. Can be called multiple times to add additional properties.
     * Automatically adds java system properties afterwards.
     * 
     * @param newProperties
     *            complete new set of properties, will be added to existing properties and overwrites already defined
     *            properties with new values. None existing properties will be added.
     */
    public abstract void setProperties(final Properties newProperties);

    /**
     * Sets a property during runtime. Overwrites an existing property with the same name. Does not re-apply any java
     * system settings.
     * 
     * @param key
     *            new property key
     * @param value
     *            new property value
     */
    public abstract void setProperty(final String key, final String value);

    /**
     * Updates the properties.
     */
    public abstract void update();

    /**
     * Returns the absolute paths to the resolved property files. This means the property files which are there by
     * default and the property files transitively included by &quot;includes&quot; in these property files. However
     * note that some of the default files are optional (as &quot;dev.properties&quot;) and the returned list only
     * contains existing files.
     * 
     * @return the resolved property files as described above
     * @deprecated For internal use only.
     */
    @Deprecated
    public abstract List<String> getResolvedPropertyFiles();
}
