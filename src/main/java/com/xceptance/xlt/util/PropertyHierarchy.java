/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class PropertyHierarchy
{
    public final String VALUE_SEPARATOR = "\n";

    private final String KEY_SEPARATOR = ".";

    private final Map<String, PropertyHierarchy> subs = new HashMap<String, PropertyHierarchy>();

    private final String key;

    private final String domain;

    private String value;

    /**
     * Creates a property with given key. The domain is set to <code>null</code>.
     * 
     * @param key
     *            property key
     */
    public PropertyHierarchy(final String key)
    {
        this(null, key);
    }

    /**
     * Create a new property with given domain and key.
     * 
     * @param domain
     *            property domain
     * @param key
     *            property key
     */
    public PropertyHierarchy(final String domain, final String key)
    {
        this.domain = domain != null ? domain : "";
        this.key = key;
    }

    /**
     * Set several next-level properties.
     * 
     * @param input
     */
    public void set(final Map<String, String> input)
    {
        if (input != null)
        {
            final Set<Entry<String, String>> entrySet = input.entrySet();

            for (final Entry<String, String> entry : entrySet)
            {
                set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Set underlying property. The key might contain property path separators and be a property chain so far.
     * 
     * @param key
     *            property key
     * @param value
     *            property value
     */
    public void set(final String key, final String value)
    {
        if (key != null)
        {
            set(getKeyHierarchy(key), value);
        }
    }

    /**
     * Set underlying property. The property is set relative to this property object. The keyHierarchy contains the path
     * in hierarchical order, starting with the highest.
     * 
     * @param keyHierarchy
     *            keys
     * @param value
     *            value
     */
    public void set(final List<String> keyHierarchy, final String value)
    {
        if (keyHierarchy != null && keyHierarchy.size() > 0)
        {
            final String key = keyHierarchy.get(0);

            // get corresponding sub or create if necessary
            PropertyHierarchy sub = subs.get(key);
            if (sub == null)
            {
                sub = new PropertyHierarchy(getKey(), key);
                subs.put(key, sub);
            }

            // update sub
            if (keyHierarchy.size() > 1)
            {
                sub.set(keyHierarchy.subList(1, keyHierarchy.size()), value);
            }
            else
            {
                sub.setValue(value);
            }
        }
    }

    /**
     * Get the addressed property hierarchy. The key might be a single fragment or a key chain relative to this level.
     * 
     * @param key
     * @return
     */
    public PropertyHierarchy get(final String key)
    {
        return get(getKeyHierarchy(key));
    }

    /**
     * Get the addressed property hierarchy. The key might be a single fragment or a key chain relative to this level.
     * 
     * @param keyHierarchy
     * @return
     */
    public PropertyHierarchy get(final List<String> keyHierarchy)
    {
        if (keyHierarchy != null && keyHierarchy.size() > 0)
        {
            final String subKey = keyHierarchy.get(0);
            final PropertyHierarchy sub = subs.get(subKey);
            if (sub != null)
            {
                if (keyHierarchy.size() > 1)
                {
                    return sub.get(keyHierarchy.subList(1, keyHierarchy.size()));
                }
                else
                {
                    return sub;
                }
            }
        }

        return null;
    }

    /**
     * Get the value for the current property.
     * 
     * @return property value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Get the value for the current property, or <code>defaultValue</code> if no value is present.
     * 
     * @param defaultValue
     * @return current value or defaultValue if current value is null
     */
    public String getValue(final String defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * Get the key fragment.
     * 
     * @return key fragment
     */
    public String getKeyFragment()
    {
        return key;
    }

    /**
     * Get the child key fragments (lower next level key fragments).
     * 
     * @return child key fragments
     */
    public Set<String> getChildKeyFragments()
    {
        return subs.keySet();
    }

    /**
     * Get the full property key.
     * 
     * @return full property key
     */
    public String getKey()
    {
        return domain.length() < 1 ? key : domain + KEY_SEPARATOR + key;
    }

    /**
     * Get the value for the current key and all sub keys if any, separated by a line break (<code>\n</code>).
     * 
     * @return the value for the current key and all sub keys if any
     */
    public String getKeyValues()
    {
        final StringBuilder sb = new StringBuilder();

        if (value != null)
        {
            sb.append(value).append(VALUE_SEPARATOR);
        }

        for (final PropertyHierarchy sub : subs.values())
        {
            sb.append(sub.getKeyValues());
        }

        return sb.toString();
    }

    /**
     * Get the value of the given key (hierarchy), starting at the current level.
     * 
     * @param key
     *            property key might be a single key or a hierarchical key chain like <code>this.is.my.key</code>
     * @return the addressed value
     */
    public String getKeyValue(final String key)
    {
        return getKeyValue(getKeyHierarchy(key));
    }

    /**
     * Get the value of the given key hierarchy, starting at the current level.
     * 
     * @param key
     *            property key might be a single key or a hierarchical key chain represented as list
     * @return the addressed value
     */
    public String getKeyValue(final List<String> keyHierarchy)
    {
        final PropertyHierarchy prop = get(keyHierarchy);
        if (prop != null)
        {
            return prop.getValue();
        }

        return null;
    }

    /**
     * Set the value.
     * 
     * @param value
     */
    public void setValue(final String value)
    {
        this.value = value;
    }

    /**
     * First trim than remove leading and trailing periods.
     * 
     * @param key
     * @return normalized key
     */
    private String normalizeKey(String key)
    {
        key = key.trim();

        if (key.startsWith(KEY_SEPARATOR))
        {
            key = key.substring(1);
        }

        if (key.endsWith(KEY_SEPARATOR))
        {
            key = key.substring(0, key.length() - 1);
        }

        return key;
    }

    /**
     * Split the string into key fragments and return hierarchical key structure, starting with the highest.
     * 
     * @return hierarchical key structure
     */
    private List<String> getKeyHierarchy(String key)
    {
        List<String> keyHierarchy = null;

        key = normalizeKey(key);

        if (key.length() > 0)
        {
            if (key.contains(KEY_SEPARATOR))
            {
                keyHierarchy = Arrays.asList(key.split("\\" + KEY_SEPARATOR));
            }
            else
            {
                // only 1 key level
                keyHierarchy = new ArrayList<String>(1);
                keyHierarchy.add(key);
            }
        }

        if (keyHierarchy == null)
        {
            keyHierarchy = Collections.emptyList();
        }

        return keyHierarchy;
    }
}
