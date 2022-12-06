/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.api.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.xceptance.xlt.api.engine.Session;
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
     * Constants to indicate the name of the base properties
     */
    public static final String DEFAULT_PROPERTIES = "DEFAULT";
    public static final String PROJECT_PROPERTIES = "PROJECT";
    public static final String TEST_PROPERTIES = "TEST";
    public static final String DEVELOPMENT_PROPERTIES = "DEVELOPMENT";
    public static final String SECRET_PROPERTIES = "SECRET";
    public static final String SYSTEM_PROPERTIES = "SYSTEM";

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
    public static void reset()
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
    public abstract Properties getCopyOfProperties();

    /**
     * Returns a reference to the properties. This is mainly here for speed. Deal with it at your
     * own discretion. You are not supposed to modify these!
     *
     * @return the properties
     */
    public abstract Properties getProperties();

    /**
     * Returns an ordered list of property sources. This allows a more tailored access if needed.
     * Don't write to these properties, because XLT will not pay any attention. This is mainly meant
     * when you want to extend the property concept in a test suite for your own pleasure.
     *
     * Please keep in mind that this is all shared across test threads, hence it is read-only which
     * is also in parts enforced.
     */
    public abstract LinkedHashMap<String, Properties> getPropertyBuckets();

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
     * is not found. This method will lookup the session from the context automatically!
     *
     * @param key
     *            the property key
     * @return the value of the key
     */
    public abstract String getProperty(final String key);

    /**
     * Searches for the property with the specified key in this property list. The method returns null if the property
     * is not found. In most cases, {@link #getProperty(String)}} will be sufficient. For testing and
     * more advanced use cases, a session context can be passed in.
     *
     * @param key
     *            the property key
     * @param session
     *            the session information to use to enhance the lookup
     * @return the value of the key
     * @since 7.0.0
     */
    public abstract String getProperty(final Session session, final String key);

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
     * Returns the effective key to be used for property lookup via one of the getProperty(...) methods.
     * <p>
     * When looking up a key, "password" for example, the following effective keys are tried, in this order:
     * <ol>
     * <li>the prefix "secret." plus the simple key to ensure precedence of secret properties over public ones</li>
     * <li>the test user name plus simple key, e.g. "TAuthor.password"</li>
     * <li>the test class name plus simple key, e.g. "com.xceptance.xlt.samples.tests.TAuthor.password"</li>
     * <li>the simple key, e.g. "password"</li>
     * </ol>
     *
     * This method has been opened so that property extensions can use this logic for their
     * own purpose if needed.
     *
     * @param session the session to get utility data from
     * @param bareKey
     *            the bare property key, i.e. without any prefixes
     * @return the first key that produces a result
     * @since 7.0.0
     */
    public abstract String getEffectiveKey(final Session session, final String bareKey);

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
     * Method for changing the properties during runtime. Can be called multiple times to add additional properties.
     * It does not apply System properties automatically anymore!!! If you need that in your logic, simply
     * run {@code #setProperties(System.getProperties()}
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
     * Clears all properties but does not do anything else. This is a dangerous operation!
     *
     * @return the cleared instance
     */
    public abstract XltProperties clear();
}
