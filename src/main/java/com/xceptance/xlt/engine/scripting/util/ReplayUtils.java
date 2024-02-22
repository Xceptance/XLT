/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * Replay utilities.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class ReplayUtils
{
    /**
     * The commands that have a value, but no target.
     */
    private static final String[] VALUE_ONLY_COMMANDS =
        {
            "assertLoadTime", "assertNotTextPresent", "assertNotTitle", "assertPageSize", "assertTextPresent", "assertTitle", "echo",
            "open", "pause", "setTimeout", "storeTitle", "waitForTextPresent", "waitForTitle", "waitForNotTextPresent", "waitForNotTitle",
        };

    private static final String INDEX = "index";

    private static final Pattern INDEX_PATTERN = Pattern.compile(".*?index=(\\d+).*?");

    private static final String INDEX_PREVALUE = "index=";

    private static final String INDEX_REGEX = INDEX_PREVALUE + "(\\d+)";

    private static final String NAME = "name";

    private static final Pattern NAME_PATTERN = Pattern.compile("^(\\S+).*?");

    private static final String VALUE = "value";

    private static final Pattern VALUE_PATTERN = Pattern.compile(".*?(value=)?(.*?)");

    private static final Pattern STYLE_ATT_SEMICOLON_REGEXP = Pattern.compile("[;]+(?=(?:(?:[^\"]*\"){2})*[^\"]*$)(?=(?:(?:[^']*'){2})*[^']*$)(?=(?:[^()]*\\([^()]*\\))*[^()]*$)");

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ReplayUtils()
    {
    }

    /**
     * Returns the given CSS style text in standardized form.
     * 
     * @param styleText
     *            the CSS style text
     * @return given CSS style text in standardized form
     */
    public static String getStandardizedStyle(final String styleText)
    {
        final String[] parts = STYLE_ATT_SEMICOLON_REGEXP.split(styleText);
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts)
        {
            final int idx = part.indexOf(':');
            if (idx > 0)
            {
                sb.append(part.substring(0, idx).toLowerCase()).append(':').append(part.substring(idx + 1)).append(';');
            }
        }

        String s = sb.toString();
        if (!s.endsWith(";"))
        {
            s += ";";
        }
        return s;
    }

    public static AttributeLocatorInfo parseAttributeLocator(final String attributeLocator)
    {
        int idx = attributeLocator.lastIndexOf('@');
        String attributeName = null;
        while (RegExUtils.isMatching((attributeName = attributeLocator.substring(idx + 1)), "^\\{\\S+\\}"))
        {
            idx = attributeLocator.lastIndexOf('@', idx - 1);
        }

        return new AttributeLocatorInfo(attributeLocator.substring(0, idx), attributeName.toLowerCase());
    }

    public static class AttributeLocatorInfo
    {
        private final String elementLocator;

        private final String attributeName;

        private AttributeLocatorInfo(final String elementLocator, final String attributeName)
        {
            this.attributeName = attributeName;
            this.elementLocator = elementLocator;
        }

        /**
         * @return the elementLocator
         */
        public String getElementLocator()
        {
            return elementLocator;
        }

        /**
         * @return the attributeName
         */
        public String getAttributeName()
        {
            return attributeName;
        }
    }

    public static Map<String, String> parseAttributes(String input)
    {
        final HashMap<String, String> results = new HashMap<String, String>();
        boolean emptyValueExpected = false;

        // name
        {
            final Matcher m = NAME_PATTERN.matcher(input);
            if (m.matches())
            {
                // parse name value
                final String value = m.group(1);
                results.put(NAME, value);

                // remove name value incl. following space (if existing)
                if (input.length() > value.length())
                {
                    input = input.substring(value.length() + 1);
                    emptyValueExpected = true;
                }
                else
                {
                    input = input.substring(value.length());
                }
            }
        }

        // index
        {
            final Matcher m = INDEX_PATTERN.matcher(input);
            if (m.matches())
            {
                // remember if input begins with 'index=' (for clean up)
                boolean leadingIndex = false;
                if (input.startsWith(INDEX_PREVALUE))
                {
                    leadingIndex = true;
                    emptyValueExpected = false;
                }

                // parse index value
                final String value = m.group(1);
                results.put(INDEX, value);
                input = input.replaceFirst(INDEX_REGEX, "");

                // clean up
                // remove index=<value> incl leading/trailing whitespace (if any) -> see above
                if (input.length() > 0)
                {
                    if (leadingIndex)
                    {
                        input = input.substring(1);
                        emptyValueExpected = true;
                    }
                    else
                    {
                        input = input.substring(0, input.length() - 1);
                    }
                }
            }
        }

        // value
        {
            if (input.length() > 0)
            {
                // parse value value
                final Matcher m = VALUE_PATTERN.matcher(input);
                if (m.matches())
                {
                    final String value = m.group(2);
                    results.put(VALUE, value);
                }
            }
            else if (emptyValueExpected)
            {
                results.put(VALUE, input);
            }
        }

        return results;
    }

    public static Map<String, String> parseStyleString(final String style)
    {
        final HashMap<String, String> cssProperties = new HashMap<String, String>();
        final String[] parts = STYLE_ATT_SEMICOLON_REGEXP.split(style);
        for (int i = 0; i < parts.length; i++)
        {
            final String part = parts[i];
            final int idx = part.indexOf(':');
            if (idx > 0 && idx < part.length() - 1)
            {
                cssProperties.put(part.substring(0, idx).toLowerCase(), part.substring(idx + 1).trim());
            }
        }

        return cssProperties;
    }

    /**
     * Parses the given coordinate string and returns a two-dimensional array of integers holding the X,Y coordinates.
     * 
     * @param coordinates
     *            the coordinate string to parse
     * @return parsed coordinates or null if it could not be parsed
     */
    public static int[] parseCoordinates(String coordinates)
    {
        if (StringUtils.isNotBlank(coordinates))
        {
            final String[] parts = coordinates.split(",");
            if (parts.length == 2)
            {
                try
                {
                    final int x = Integer.parseInt(parts[0].trim());
                    final int y = Integer.parseInt(parts[1].trim());

                    return new int[]
                        {
                            x, y
                        };
                }
                catch (final NumberFormatException nfe)
                {
                    XltLogger.runTimeLogger.error("Failed to parse coordinates", nfe);
                }
            }
        }
        return null;
    }

    /**
     * Returns whether or not the given command name denotes a value-only command.
     * 
     * @param name
     *            the of the command in question
     * @return <code>true</code> if the given command name denotes a value-only command, <code>false</code> otherwise
     */
    public static boolean isValueOnlyCommand(String name)
    {
        for (final String s : VALUE_ONLY_COMMANDS)
        {
            if (name.equals(s))
            {
                return true;
            }
        }
        return false;
    }

}
