/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package util.xlt.properties;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * This class is designed for changes to the XltProperties that can be reverted for example after a test has been
 * finished. Changes to the properties should be reverted after a test to avoid side effects on other tests. The usage
 * is as follows:
 * <ol>
 * <li>Get an instance</li>
 * <li>Call {@link #apply()}</li>
 * <li>Call {@link #reverse()}</li>
 * </ol>
 * This class is not thread-safe but that only matters if there are different reversible changes with the same key at
 * the same time.
 * 
 * @author Sebastian Oerding
 */
public class ReversibleChange
{
    private final boolean hadKey;

    private final String key;

    private final String oldValue;

    private final String newValue;

    /**
     * Initializes a new instance with the argument values. Does NOT cause any changes to the properties.
     * 
     * @see #apply
     * @see #reverse
     */
    public ReversibleChange(final String key, final String newValue)
    {
        hadKey = XltProperties.getInstance().containsKey(key);
        this.key = key;
        oldValue = XltProperties.getInstance().getProperty(key);
        this.newValue = newValue;
    }

    /**
     * Applies the change to the singleton XltProperties instance.
     * <p>
     * Sets the property according to the arguments given to the constructor.
     * </p>
     */
    public void apply()
    {
        XltProperties.getInstance().setProperty(key, newValue);
    }

    /**
     * Reverses the change of the singleton XltProperties instance.
     * <p>
     * Restores the old property value (which was there before {@link #apply()} is called) or if there was no such
     * property the property is completely removed.
     * </p>
     */
    public void reverse()
    {
        if (hadKey)
        {
            XltProperties.getInstance().setProperty(key, oldValue);
        }
        else
        {
            XltProperties.getInstance().removeProperty(key);
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Key: ");
        sb.append(key);
        sb.append(", new value: ");
        sb.append(newValue);
        if (!hadKey)
        {
            sb.append(", previously there was no property with the key.");
        }
        else
        {
            sb.append(", old value: ");
            sb.append(oldValue);
        }
        return sb.toString();
    }
}
